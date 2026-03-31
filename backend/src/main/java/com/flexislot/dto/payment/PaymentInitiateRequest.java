package com.flexislot.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "Booking ID is required")
    @NotBlank
    private String bookingId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    @NotBlank(message = "Provider is required")
    private String provider;
}
