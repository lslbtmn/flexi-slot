package com.flexislot.service;

import com.flexislot.domain.Booking;
import com.flexislot.domain.Slot;
import com.flexislot.domain.enums.BookingStatus;
import com.flexislot.domain.enums.PaymentStatusEnum;
import com.flexislot.domain.enums.SlotStatus;
import com.flexislot.dto.booking.BookingRequest;
import com.flexislot.dto.booking.BookingResponse;
import com.flexislot.dto.common.PageResponse;
import com.flexislot.exception.AppException;
import com.flexislot.exception.ForbiddenException;
import com.flexislot.exception.ResourceNotFoundException;
import com.flexislot.repository.BookingRepository;
import com.flexislot.repository.BusinessRepository;
import com.flexislot.repository.ServiceRepository;
import com.flexislot.repository.SlotRepository;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.pricing.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final CustomerService customerService;
    private final PricingService pricingService;

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse create(BookingRequest request, UserPrincipal principal) {
        String customerId = customerService.getCustomerIdForUser(principal);
        if (customerId == null) {
            throw new AppException("Customer profile required. Create customer profile first.", HttpStatus.BAD_REQUEST);
        }
        Slot slot = slotRepository.findByIdWithPessimisticLock(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + request.getSlotId()));
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new AppException("Slot is not available for booking", HttpStatus.BAD_REQUEST);
        }
        if (bookingRepository.existsBySlotId(slot.getId())) {
            throw new AppException("Slot is already booked", HttpStatus.CONFLICT);
        }

        com.flexislot.domain.Service service = serviceRepository.findById(slot.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + slot.getServiceId()));
        long totalSlots = slotRepository.countByServiceId(service.getId());
        long bookedSlots = slotRepository.countByServiceIdAndStatus(service.getId(), SlotStatus.BOOKED);
        java.math.BigDecimal realTimePrice = Boolean.TRUE.equals(service.getDynamicPricingEnabled()) ? 
                pricingService.calculatePrice(service.getBasePrice(), bookedSlots, totalSlots) : 
                service.getBasePrice();

        slot.setStatus(SlotStatus.BOOKED);
        slot.setPrice(realTimePrice);
        slotRepository.save(slot);
        Booking booking = Booking.builder()
                .customerId(customerId)
                .slotId(slot.getId())
                .bookingStatus(BookingStatus.CONFIRMED)
                .paymentStatus(PaymentStatusEnum.INITIATED)
                .bookingTimestamp(Instant.now())
                .price(realTimePrice)
                .build();
        booking = bookingRepository.save(booking);
        log.info("Created booking {} for slot {}", booking.getId(), slot.getId());
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<BookingResponse> getByCustomerId(String customerId, Pageable pageable,
            UserPrincipal principal) {
        String principalCustomerId = customerService.getCustomerIdForUser(principal);
        if (principalCustomerId == null || !principalCustomerId.equals(customerId)) {
            throw new ForbiddenException("Not allowed to view these bookings");
        }
        Page<Booking> page = bookingRepository.findByCustomerIdOrderByBookingTimestampDesc(customerId, pageable);
        return PageResponse.<BookingResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse cancel(String id, UserPrincipal principal) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        String principalCustomerId = customerService.getCustomerIdForUser(principal);
        if (principalCustomerId == null || !booking.getCustomerId().equals(principalCustomerId)) {
            throw new ForbiddenException("Not allowed to cancel this booking");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new AppException("Booking is already cancelled", HttpStatus.BAD_REQUEST);
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        Slot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            slotRepository.save(slot);
        }
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking b) {
        Slot slot = slotRepository.findById(b.getSlotId()).orElse(null);
        com.flexislot.domain.Service service = slot != null ? serviceRepository.findById(slot.getServiceId()).orElse(null) : null;
        com.flexislot.domain.Business business = service != null ? businessRepository.findById(service.getBusinessId()).orElse(null) : null;

        return BookingResponse.builder()
                .id(b.getId())
                .customerId(b.getCustomerId())
                .slotId(b.getSlotId())
                .serviceName(service != null ? service.getServiceName() : null)
                .businessName(business != null ? business.getName() : null)
                .location(business != null ? business.getLocation() : null)
                .slotDate(slot != null ? slot.getSlotDate() : null)
                .startTime(slot != null ? slot.getStartTime() : null)
                .endTime(slot != null ? slot.getEndTime() : null)
                .bookingStatus(b.getBookingStatus().name())
                .paymentStatus(b.getPaymentStatus().name())
                .bookingTimestamp(b.getBookingTimestamp())
                .createdAt(b.getCreatedAt())
                .price(b.getPrice())
                .build();
    }
}
