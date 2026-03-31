package com.flexislot.dto.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessResponse {

    private String id;
    private String ownerUserId;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String serviceType;
    /**
     * JSON string representing operating hours (stored as LONGTEXT).
     */
    private String operatingHours;
    private Instant createdAt;
    private Instant updatedAt;
}
