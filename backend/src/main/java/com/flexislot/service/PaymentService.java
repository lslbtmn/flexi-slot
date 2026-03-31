package com.flexislot.service;

import com.flexislot.domain.Payment;
import com.flexislot.domain.enums.PaymentStatusEnum;
import com.flexislot.dto.payment.PaymentInitiateRequest;
import com.flexislot.dto.payment.PaymentResponse;
import com.flexislot.exception.ResourceNotFoundException;
import com.flexislot.repository.BookingRepository;
import com.flexislot.repository.PaymentRepository;
import com.flexislot.domain.Booking;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Transactional
    public PaymentResponse initiate(PaymentInitiateRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        String email = booking.getCustomer().getEmail();
        long amountInKobo = request.getAmount().multiply(new java.math.BigDecimal("100")).longValue();

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .provider("PAYSTACK")
                .status(PaymentStatusEnum.INITIATED)
                .build();
        payment = paymentRepository.save(payment);

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "amount", amountInKobo,
                    "reference", "FS-" + payment.getId(),
                    "callback_url", "http://localhost:5173/payment/callback"
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.paystack.co/transaction/initialize"))
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());

            if (jsonNode.has("status") && jsonNode.get("status").asBoolean()) {
                JsonNode data = jsonNode.get("data");
                payment.setProviderReference(data.get("reference").asText());
                paymentRepository.save(payment);

                PaymentResponse res = toResponse(payment);
                res.setAuthorizationUrl(data.get("authorization_url").asText());
                return res;
            } else {
                log.error("Paystack initialization failed: {}", response.body());
                throw new RuntimeException("Payment provider initialization failed");
            }
        } catch (Exception e) {
            log.error("Error communicating with Paystack", e);
            throw new RuntimeException("Error communicating with payment provider", e);
        }
    }

    @Transactional
    public PaymentResponse verifyTransaction(String reference) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.paystack.co/transaction/verify/" + reference))
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());

            Payment payment = paymentRepository.findByProviderReference(reference)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference: " + reference));

            if (jsonNode.has("status") && jsonNode.get("status").asBoolean()) {
                JsonNode data = jsonNode.get("data");
                String status = data.get("status").asText();

                if ("success".equals(status)) {
                    payment.setStatus(PaymentStatusEnum.SUCCESS);
                    payment.setPaidAt(java.time.Instant.now());
                    
                    Booking booking = payment.getBooking();
                    if (booking != null) {
                        booking.setPaymentStatus(PaymentStatusEnum.SUCCESS);
                        bookingRepository.save(booking);
                    }
                } else {
                    payment.setStatus(PaymentStatusEnum.FAILED);
                }
            } else {
                payment.setStatus(PaymentStatusEnum.FAILED);
            }
            payment = paymentRepository.save(payment);
            return toResponse(payment);
        } catch (Exception e) {
            log.error("Error verifying with Paystack", e);
            throw new RuntimeException("Error verifying transaction", e);
        }
    }

    @Transactional
    public PaymentResponse markSuccess(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        payment.setStatus(PaymentStatusEnum.SUCCESS);
        payment.setPaidAt(java.time.Instant.now());
        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse markFailed(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        payment.setStatus(PaymentStatusEnum.FAILED);
        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .bookingId(p.getBookingId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .provider(p.getProvider())
                .providerReference(p.getProviderReference())
                .status(p.getStatus().name())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
