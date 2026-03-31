package com.flexislot.service;

import com.flexislot.domain.Slot;
import com.flexislot.domain.enums.SlotStatus;
import com.flexislot.dto.common.PageResponse;
import com.flexislot.dto.slot.BulkSlotRequest;
import com.flexislot.dto.slot.SlotRequest;
import com.flexislot.dto.slot.SlotResponse;
import com.flexislot.exception.ForbiddenException;
import com.flexislot.exception.ResourceNotFoundException;
import com.flexislot.repository.BusinessRepository;
import com.flexislot.repository.ServiceRepository;
import com.flexislot.repository.SlotRepository;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.pricing.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;
    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final PricingService pricingService;

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public SlotResponse create(SlotRequest request, UserPrincipal principal) {
        com.flexislot.domain.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + request.getServiceId()));
        String ownerBusinessId = businessRepository.findByOwnerUserId(principal.getId())
                .map(com.flexislot.domain.Business::getId)
                .orElseThrow(() -> new ForbiddenException("Business not found"));
        if (!service.getBusinessId().equals(ownerBusinessId)) {
            throw new ForbiddenException("Not allowed to create slots for this service");
        }
        long totalSlots = slotRepository.countByServiceId(service.getId());
        long bookedSlots = slotRepository.countByServiceIdAndStatus(service.getId(), SlotStatus.BOOKED);
        java.math.BigDecimal price = Boolean.TRUE.equals(service.getDynamicPricingEnabled()) ? 
                pricingService.calculatePrice(service.getBasePrice(), bookedSlots, totalSlots) : 
                service.getBasePrice();
        Slot slot = Slot.builder()
                .serviceId(service.getId())
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(price)
                .status(SlotStatus.AVAILABLE)
                .build();
        slot = slotRepository.save(slot);
        return toResponse(slot);
    }

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public List<SlotResponse> generateBulk(BulkSlotRequest request, UserPrincipal principal) {
        com.flexislot.domain.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + request.getServiceId()));

        com.flexislot.domain.Business business = businessRepository.findByOwnerUserId(principal.getId())
                .orElseThrow(() -> new ForbiddenException("Business not found"));

        if (!service.getBusinessId().equals(business.getId())) {
            throw new ForbiddenException("Not allowed to create slots for this service");
        }

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        if (business.getOperatingHours() != null && !business.getOperatingHours().isBlank()) {
            try {
                String[] parts = business.getOperatingHours().split("-");
                if (parts.length == 2) {
                    startTime = LocalTime.parse(parts[0].trim());
                    endTime = LocalTime.parse(parts[1].trim());
                }
            } catch (DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                log.warn("Failed to parse operating hours '{}' for business {}. Using default 09:00-17:00",
                        business.getOperatingHours(), business.getId());
            }
        }

        if (endTime.isBefore(startTime)) {
            endTime = LocalTime.of(23, 59);
        }

        int durationMinutes = service.getDurationMinutes();
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Service duration must be greater than 0");
        }

        long totalSlots = slotRepository.countByServiceId(service.getId());
        long bookedSlots = slotRepository.countByServiceIdAndStatus(service.getId(), SlotStatus.BOOKED);

        List<Slot> slotsToCreate = new ArrayList<>();
        LocalTime currentStartTime = startTime;

        while (currentStartTime.plusMinutes(durationMinutes).isBefore(endTime)
                || currentStartTime.plusMinutes(durationMinutes).equals(endTime)) {
            LocalTime currentEndTime = currentStartTime.plusMinutes(durationMinutes);

            boolean exists = slotRepository.existsByServiceIdAndSlotDateAndStartTimeAndEndTime(
                    service.getId(), request.getSlotDate(), currentStartTime, currentEndTime);

            if (!exists) {
                java.math.BigDecimal price = Boolean.TRUE.equals(service.getDynamicPricingEnabled()) ? 
                        pricingService.calculatePrice(service.getBasePrice(), bookedSlots, totalSlots) : 
                        service.getBasePrice();

                Slot slot = Slot.builder()
                        .serviceId(service.getId())
                        .slotDate(request.getSlotDate())
                        .startTime(currentStartTime)
                        .endTime(currentEndTime)
                        .price(price)
                        .status(SlotStatus.AVAILABLE)
                        .build();
                slotsToCreate.add(slot);
                totalSlots++;
            }
            currentStartTime = currentEndTime;
        }

        List<Slot> savedSlots = slotRepository.saveAll(slotsToCreate);
        return savedSlots.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<SlotResponse> getAvailableByServiceId(String serviceId, LocalDate fromDate, LocalDate toDate,
            Pageable pageable) {
        Page<Slot> page;
        if (fromDate != null || toDate != null) {
            page = slotRepository.findByServiceIdAndStatusAndDateRange(
                    serviceId, SlotStatus.AVAILABLE, fromDate, toDate, pageable);
        } else {
            page = slotRepository.findByServiceIdAndStatus(serviceId, SlotStatus.AVAILABLE, pageable);
        }

        com.flexislot.domain.Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service != null && Boolean.TRUE.equals(service.getDynamicPricingEnabled())) {
            long totalSlots = slotRepository.countByServiceId(service.getId());
            long bookedSlots = slotRepository.countByServiceIdAndStatus(service.getId(), SlotStatus.BOOKED);
            java.math.BigDecimal realTimePrice = pricingService.calculatePrice(service.getBasePrice(), bookedSlots,
                    totalSlots);

            // Override the static DB price with the real-time calculated price
            page.getContent().forEach(slot -> slot.setPrice(realTimePrice));
        } else if (service != null) {
            page.getContent().forEach(slot -> slot.setPrice(service.getBasePrice()));
        }

        return PageResponse.<SlotResponse>builder()
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
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public SlotResponse update(String id, SlotRequest request, UserPrincipal principal) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + id));
        String ownerBusinessId = businessRepository.findByOwnerUserId(principal.getId())
                .map(com.flexislot.domain.Business::getId)
                .orElseThrow(() -> new ForbiddenException("Business not found"));
        com.flexislot.domain.Service service = serviceRepository.findById(slot.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        if (!service.getBusinessId().equals(ownerBusinessId)) {
            throw new ForbiddenException("Not allowed to update this slot");
        }
        slot.setServiceId(request.getServiceId());
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        long totalSlots = slotRepository.countByServiceId(service.getId());
        long bookedSlots = slotRepository.countByServiceIdAndStatus(service.getId(), SlotStatus.BOOKED);
        java.math.BigDecimal realTimePrice = Boolean.TRUE.equals(service.getDynamicPricingEnabled()) ? 
                pricingService.calculatePrice(service.getBasePrice(), bookedSlots, totalSlots) : 
                service.getBasePrice();
        slot.setPrice(realTimePrice);
        slot = slotRepository.save(slot);
        return toResponse(slot);
    }

    @Transactional
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    public void delete(String id, UserPrincipal principal) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + id));
        if (principal.getRole() != com.flexislot.domain.enums.UserRole.ADMIN) {
            String ownerBusinessId = businessRepository.findByOwnerUserId(principal.getId())
                    .map(com.flexislot.domain.Business::getId)
                    .orElse(null);
            com.flexislot.domain.Service service = serviceRepository.findById(slot.getServiceId()).orElse(null);
            if (service == null || !service.getBusinessId().equals(ownerBusinessId)) {
                throw new ForbiddenException("Not allowed to delete this slot");
            }
        }
        slotRepository.delete(slot);
    }

    private SlotResponse toResponse(Slot s) {
        return SlotResponse.builder()
                .id(s.getId())
                .serviceId(s.getServiceId())
                .slotDate(s.getSlotDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .price(s.getPrice())
                .basePrice(s.getPrice()) // default fallback, UI fetches actual basePrice directly
                .status(s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
