package com.flexislot.controller;

import com.flexislot.dto.common.PageResponse;
import com.flexislot.dto.slot.BulkSlotRequest;
import com.flexislot.dto.slot.SlotRequest;
import com.flexislot.dto.slot.SlotResponse;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Slot management and listing")
public class SlotController {

    private final SlotService slotService;

    @PostMapping
    @Operation(summary = "Create slot (BUSINESS_OWNER)")
    public ResponseEntity<SlotResponse> create(@Valid @RequestBody SlotRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SlotResponse response = slotService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Generate bulk slots for for a date (BUSINESS_OWNER)")
    public ResponseEntity<List<SlotResponse>> createBulk(@Valid @RequestBody BulkSlotRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<SlotResponse> response = slotService.generateBulk(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/service/{serviceId}")
    @Operation(summary = "Get available slots for service (paginated, optional date range)")
    public ResponseEntity<PageResponse<SlotResponse>> getByServiceId(
            @PathVariable String serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(slotService.getAvailableByServiceId(serviceId, fromDate, toDate, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update slot (owner only)")
    public ResponseEntity<SlotResponse> update(@PathVariable String id,
            @Valid @RequestBody SlotRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(slotService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete slot")
    public ResponseEntity<Void> delete(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        slotService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
