package com.flexislot.controller;

import com.flexislot.dto.customer.CustomerRequest;
import com.flexislot.dto.customer.CustomerResponse;
import com.flexislot.security.UserPrincipal;
import com.flexislot.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer profile management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create customer profile (CUSTOMER)")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        CustomerResponse response = customerService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getById(@PathVariable String id,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.getById(id, principal));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's customer profile")
    public ResponseEntity<CustomerResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.getCurrentCustomer(principal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer (owner only)")
    public ResponseEntity<CustomerResponse> update(@PathVariable String id,
                                                    @Valid @RequestBody CustomerRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        customerService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
