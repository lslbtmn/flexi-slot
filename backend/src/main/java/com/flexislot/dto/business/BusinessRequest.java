package com.flexislot.dto.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private String phone;
    private String location;
    private String serviceType;
    /**
     * JSON string representing operating hours (stored as LONGTEXT).
     */
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Operating hours must be in HH:mm-HH:mm format (e.g., 09:00-17:00)")
    private String operatingHours;
}
