package com.flexislot.domain;

import com.flexislot.domain.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "slot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot extends BaseEntity {

    @Column(name = "service_id", nullable = false, length = 26)
    private String serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private Service service;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;
}
