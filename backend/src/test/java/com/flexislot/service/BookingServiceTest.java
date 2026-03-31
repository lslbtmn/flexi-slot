package com.flexislot.service;

import com.flexislot.domain.Booking;
import com.flexislot.domain.Customer;
import com.flexislot.domain.Slot;
import com.flexislot.domain.enums.BookingStatus;
import com.flexislot.domain.enums.PaymentStatusEnum;
import com.flexislot.domain.enums.SlotStatus;
import com.flexislot.dto.booking.BookingRequest;
import com.flexislot.dto.booking.BookingResponse;
import com.flexislot.exception.AppException;
import com.flexislot.repository.BookingRepository;
import com.flexislot.repository.SlotRepository;
import com.flexislot.security.UserPrincipal;
import com.flexislot.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private BookingService bookingService;

    private UserPrincipal customerPrincipal;
    private Slot availableSlot;
    private String customerId = "01ARZ3NDEKTSV4RRFFQ69G5FAV";

    @BeforeEach
    void setUp() {
        customerPrincipal = UserPrincipal.builder()
                .id("01ARZ3NDEKTSV4RRFFQ69G5FAV")
                .email("customer@test.com")
                .passwordHash("hash")
                .role(UserRole.CUSTOMER)
                .build();
        availableSlot = Slot.builder()
                .serviceId("01ARZ3NDEKTSV4RRFFQ69G5FCV")
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .price(java.math.BigDecimal.TEN)
                .status(SlotStatus.AVAILABLE)
                .build();
        availableSlot.setId("01ARZ3NDEKTSV4RRFFQ69G5FBV");
    }

    @Test
    void create_whenCustomerProfileMissing_throwsAppException() {
        when(customerService.getCustomerIdForUser(customerPrincipal)).thenReturn(null);
        BookingRequest request = new BookingRequest(availableSlot.getId());
        assertThrows(AppException.class, () ->
                bookingService.create(request, customerPrincipal));
        verify(slotRepository, never()).findByIdWithPessimisticLock(any());
    }

    @Test
    void create_whenSlotNotAvailable_throwsAppException() {
        when(customerService.getCustomerIdForUser(customerPrincipal)).thenReturn(customerId);
        availableSlot.setStatus(SlotStatus.BOOKED);
        when(slotRepository.findByIdWithPessimisticLock(availableSlot.getId())).thenReturn(Optional.of(availableSlot));
        BookingRequest request = new BookingRequest(availableSlot.getId());
        assertThrows(AppException.class, () ->
                bookingService.create(request, customerPrincipal));
    }
}
