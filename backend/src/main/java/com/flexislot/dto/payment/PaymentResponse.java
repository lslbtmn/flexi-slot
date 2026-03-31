package com.flexislot.dto.payment;

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
public class PaymentResponse {

    private String id;
    private String bookingId;
    private BigDecimal amount;
    private String currency;
    private String provider;
    private String providerReference;
    private String status;
    private Instant paidAt;
    private Instant createdAt;
    private String authorizationUrl;
}
