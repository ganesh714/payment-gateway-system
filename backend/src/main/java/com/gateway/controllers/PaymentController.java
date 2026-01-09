package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.models.Payment;
import com.gateway.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Map<String, Object> payload,
            Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        Payment payment = paymentService.createPayment(merchant, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId, Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        Payment payment = paymentService.getPayment(paymentId);

        if (!payment.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }

        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Payment>> getPayments(Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        return ResponseEntity.ok(paymentService.getPayments(merchant));
    }

    @PostMapping("/public")
    public ResponseEntity<Payment> createPaymentPublic(@RequestBody Map<String, Object> payload) {
        // Public endpoint for checkout page - no merchant auth required here, but valid
        // order_id check is in service
        Payment payment = paymentService.createPayment(null, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{paymentId}/public")
    public ResponseEntity<Payment> getPaymentPublic(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
