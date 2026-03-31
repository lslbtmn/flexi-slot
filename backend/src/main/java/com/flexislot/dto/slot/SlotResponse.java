package com.flexislot.dto.slot;

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
public class SlotResponse {

    private String id;
    private String serviceId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private BigDecimal basePrice;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
