package com.flexislot.dto.slot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkSlotRequest {

    @NotBlank(message = "Service ID is required")
    private String serviceId;

    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;
}
