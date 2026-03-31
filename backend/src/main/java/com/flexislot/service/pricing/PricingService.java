package com.flexislot.service.pricing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private static final BigDecimal HIGH_UTILIZATION_THRESHOLD = new BigDecimal("80");
    private static final BigDecimal LOW_UTILIZATION_THRESHOLD = new BigDecimal("40");
    private static final BigDecimal HIGH_MULTIPLIER = new BigDecimal("1.3");
    private static final BigDecimal LOW_MULTIPLIER = new BigDecimal("0.7");
    private static final int SCALE = 2;

    /**
     * Calculates dynamic price based on utilization for a service.
     * utilization > 80% => price = basePrice * 1.3
     * utilization 40%-80% => price = basePrice
     * utilization < 40% => price = basePrice * 0.7
     */

    public BigDecimal calculatePrice(BigDecimal basePrice, long bookedSlots, long totalSlots) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice != null ? basePrice : BigDecimal.ZERO;
        }
        if (totalSlots <= 0) {
            return basePrice.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal utilization = BigDecimal.valueOf(bookedSlots)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSlots), SCALE, RoundingMode.HALF_UP);
        BigDecimal price;
        if (utilization.compareTo(HIGH_UTILIZATION_THRESHOLD) > 0) {
            price = basePrice.multiply(HIGH_MULTIPLIER);
        } else if (utilization.compareTo(LOW_UTILIZATION_THRESHOLD) < 0) {
            price = basePrice.multiply(LOW_MULTIPLIER);
        } else {
            price = basePrice;
        }
        return price.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
