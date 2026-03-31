package com.flexislot.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false, unique = true, length = 26)
    private String ownerUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", insertable = false, updatable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 500)
    private String location;

    @Column(name = "service_type", length = 100)
    private String serviceType;

    @Lob
    @Column(name = "operating_hours", columnDefinition = "TEXT")
    private String operatingHours;
}
