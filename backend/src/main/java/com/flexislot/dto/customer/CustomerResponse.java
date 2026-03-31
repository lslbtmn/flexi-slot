package com.flexislot.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private String id;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
}
