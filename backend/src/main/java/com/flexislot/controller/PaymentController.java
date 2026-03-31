package com.flexislot.controller;

import com.flexislot.dto.payment.PaymentInitiateRequest;
import com.flexislot.dto.payment.PaymentResponse;
import com.flexislot.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment flow (mock)")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment (creates payment with INITIATED status)")
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitiateRequest request) {
        PaymentResponse response = paymentService.initiate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/success")
    @Operation(summary = "Mock payment success")
    public ResponseEntity<PaymentResponse> success(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.markSuccess(id));
    }

    @PutMapping("/{id}/fail")
    @Operation(summary = "Mock payment failure")
    public ResponseEntity<PaymentResponse> fail(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.markFailed(id));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify Paystack transaction")
    public ResponseEntity<PaymentResponse> verify(@RequestParam String reference) {
        return ResponseEntity.ok(paymentService.verifyTransaction(reference));
    }
}
