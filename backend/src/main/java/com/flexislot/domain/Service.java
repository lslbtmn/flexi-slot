package com.flexislot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service extends BaseEntity {

    @Column(name = "business_id", nullable = false, length = 26)
    private String businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "dynamic_pricing_enabled", nullable = false)
    @Builder.Default
    private Boolean dynamicPricingEnabled = true;
}
