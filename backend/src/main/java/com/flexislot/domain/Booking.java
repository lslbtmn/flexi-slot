package com.flexislot.domain;

import com.flexislot.domain.enums.BookingStatus;
import com.flexislot.domain.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Column(name = "customer_id", nullable = false, length = 26)
    private String customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @Column(name = "slot_id", nullable = false, unique = true, length = 26)
    private String slotId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", insertable = false, updatable = false)
    private Slot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 50)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatusEnum paymentStatus;

    @Column(name = "booking_timestamp", nullable = false)
    private Instant bookingTimestamp;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;
}
