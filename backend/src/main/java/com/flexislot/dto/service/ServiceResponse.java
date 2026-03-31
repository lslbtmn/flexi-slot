package com.flexislot.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {

    private String id;
    private String businessId;
    private String serviceName;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private Boolean dynamicPricingEnabled;
    private Instant createdAt;
    private Instant updatedAt;
}
