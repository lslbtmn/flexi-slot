package com.flexislot.controller;

import com.flexislot.dto.common.PageResponse;
import com.flexislot.dto.service.ServiceRequest;
import com.flexislot.dto.service.ServiceResponse;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.ServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Service catalog (per business)")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    @PostMapping
    @Operation(summary = "Create service (BUSINESS_OWNER)")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ServiceResponse response = serviceCatalogService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID")
    public ResponseEntity<ServiceResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(serviceCatalogService.getById(id));
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "List services by business ID (paginated)")
    public ResponseEntity<PageResponse<ServiceResponse>> getByBusinessId(
            @PathVariable String businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(serviceCatalogService.getByBusinessId(businessId, pageable, principal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update service (owner only)")
    public ResponseEntity<ServiceResponse> update(@PathVariable String id,
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(serviceCatalogService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete service")
    public ResponseEntity<Void> delete(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        serviceCatalogService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
