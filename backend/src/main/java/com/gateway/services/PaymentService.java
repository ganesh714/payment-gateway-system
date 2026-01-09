package com.gateway.services;

import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.repositories.OrderRepository;
import com.gateway.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

@Service
public class PaymentService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Random RANDOM = new Random();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ValidationService validationService;

    @Value("${gateway.simulation.upi-success-rate:0.90}")
    private double upiSuccessRate;

    @Value("${gateway.simulation.card-success-rate:0.95}")
    private double cardSuccessRate;

    @Value("${gateway.simulation.delay-min:5000}")
    private int delayMin;

    @Value("${gateway.simulation.delay-max:10000}")
    private int delayMax;

    @Value("${gateway.simulation.test-mode:false}")
    private boolean testMode;

    @Value("${gateway.simulation.test-payment-success:true}")
    private boolean testPaymentSuccess;

    @Value("${gateway.simulation.test-processing-delay:1000}")
    private int testProcessingDelay;

    public Payment createPayment(Merchant merchant, Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order_id is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Validate if order belongs to the merchant (if merchant is passed - for public
        // endpoints it might not be)
        // But for authenticated endpoints it must match.
        // Logic: if merchant is not null (Auth endpoint), check ownership.
        // If merchant is null (Public endpoint), we trust the order_id for now as per
        // requirements "validate order_id belongs to a valid merchant"
        if (merchant != null && !order.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for this merchant");
        }

        String method = (String) payload.get("method");
        if (!"upi".equals(method) && !"card".equals(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method");
        }

        Payment payment = new Payment();
        payment.setId(generatePaymentId());
        payment.setOrderId(orderId);
        payment.setMerchantId(order.getMerchantId()); // Use order's merchant ID
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus("processing"); // Initial status

        if ("upi".equals(method)) {
            String vpa = (String) payload.get("vpa");
            if (!validationService.validateVPA(vpa)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VPA");
            }
            payment.setVpa(vpa);
        } else {
            Map<String, String> card = (Map<String, String>) payload.get("card");
            if (card == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card details required");
            }
            String number = card.get("number");
            String expMonth = card.getOrDefault("expiry_month", "");
            String expYear = card.getOrDefault("expiry_year", "");

            if (!validationService.validateLuhn(number)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Card Number");
            }
            if (!validationService.validateExpiry(expMonth, expYear)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card Expired or Invalid Date");
            }

            payment.setCardNetwork(validationService.detectCardNetwork(number));
            payment.setCardLast4(number.length() >= 4 ? number.substring(number.length() - 4) : number);
        }

        // Save initial processing state
        payment = paymentRepository.save(payment);

        // Process Synchronously (Simulate)
        simulateProcessing(payment);

        return paymentRepository.save(payment);
    }

    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void simulateProcessing(Payment payment) {
        try {
            int delay;
            boolean isSuccess;

            if (testMode) {
                delay = testProcessingDelay;
                isSuccess = testPaymentSuccess;
            } else {
                delay = RANDOM.nextInt(delayMax - delayMin) + delayMin;
                double rate = "upi".equals(payment.getMethod()) ? upiSuccessRate : cardSuccessRate;
                isSuccess = RANDOM.nextDouble() < rate;
            }

            Thread.sleep(delay);

            if (isSuccess) {
                payment.setStatus("success");
            } else {
                payment.setStatus("failed");
                payment.setErrorCode("PAYMENT_FAILED");
                payment.setErrorDescription("Payment could not be processed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public java.util.List<Payment> getPayments(Merchant merchant) {
        return paymentRepository.findByMerchantId(merchant.getId());
    }

    private String generatePaymentId() {
        StringBuilder sb = new StringBuilder("pay_");
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
