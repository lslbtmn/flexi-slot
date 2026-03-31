package com.flexislot.controller;

import com.flexislot.dto.booking.BookingRequest;
import com.flexislot.dto.booking.BookingResponse;
import com.flexislot.dto.common.PageResponse;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create booking (CUSTOMER)")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse response = bookingService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List bookings by customer (paginated)")
    public ResponseEntity<PageResponse<BookingResponse>> getByCustomerId(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getByCustomerId(customerId, pageable, principal));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking (CUSTOMER)")
    public ResponseEntity<BookingResponse> cancel(@PathVariable String id,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.cancel(id, principal));
    }
}
