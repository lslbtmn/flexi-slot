package com.flexislot.service.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void calculatePrice_highUtilization_returnsBasePriceTimesOnePointThree() {
        BigDecimal basePrice = new BigDecimal("100.00");
        long bookedSlots = 90;
        long totalSlots = 100;
        BigDecimal result = pricingService.calculatePrice(basePrice, bookedSlots, totalSlots);
        assertEquals(new BigDecimal("130.00"), result);
    }

    @Test
    void calculatePrice_lowUtilization_returnsBasePriceTimesZeroPointSeven() {
        BigDecimal basePrice = new BigDecimal("100.00");
        long bookedSlots = 30;
        long totalSlots = 100;
        BigDecimal result = pricingService.calculatePrice(basePrice, bookedSlots, totalSlots);
        assertEquals(new BigDecimal("70.00"), result);
    }

    @Test
    void calculatePrice_mediumUtilization_returnsBasePrice() {
        BigDecimal basePrice = new BigDecimal("100.00");
        long bookedSlots = 50;
        long totalSlots = 100;
        BigDecimal result = pricingService.calculatePrice(basePrice, bookedSlots, totalSlots);
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void calculatePrice_zeroTotalSlots_returnsBasePrice() {
        BigDecimal basePrice = new BigDecimal("100.00");
        BigDecimal result = pricingService.calculatePrice(basePrice, 0, 0);
        assertEquals(new BigDecimal("100.00"), result);
    }
}
