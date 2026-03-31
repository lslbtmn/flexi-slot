package com.flexislot.controller;

import com.flexislot.dto.business.BusinessRequest;
import com.flexislot.dto.business.BusinessResponse;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.flexislot.dto.common.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
@Tag(name = "Business", description = "Business profile management")
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    @Operation(summary = "Create business (BUSINESS_OWNER)")
    public ResponseEntity<BusinessResponse> create(@Valid @RequestBody BusinessRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        BusinessResponse response = businessService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business by ID")
    public ResponseEntity<BusinessResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(businessService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all businesses")
    public ResponseEntity<PageResponse<BusinessResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(businessService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's business (BUSINESS_OWNER)")
    public ResponseEntity<BusinessResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(businessService.getByOwnerId(principal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update business (owner only)")
    public ResponseEntity<BusinessResponse> update(@PathVariable String id,
            @Valid @RequestBody BusinessRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(businessService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete business")
    public ResponseEntity<Void> delete(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        businessService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
