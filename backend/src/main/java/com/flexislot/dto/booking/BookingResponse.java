package com.flexislot.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private String id;
    private String customerId;
    private String slotId;
    private String serviceName;
    private String businessName;
    private String location;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String bookingStatus;
    private String paymentStatus;
    private Instant bookingTimestamp;
    private Instant createdAt;
    private BigDecimal price;
}
