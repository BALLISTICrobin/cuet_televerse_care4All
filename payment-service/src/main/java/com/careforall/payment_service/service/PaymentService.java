package com.careforall.payment_service.service;

import com.careforall.payment_service.config.RabbitMQConfig;
import com.careforall.payment_service.model.Payment;
import com.careforall.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void processWebhook(Map<String, Object> payload) {
        String webhookId = (String) payload.get("webhook_id");
        String pledgeId = (String) payload.get("pledge_id");
        String newStatus = (String) payload.get("status"); // e.g., CAPTURED
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        // 1. IDEMPOTENCY CHECK
        if (paymentRepository.findByWebhookId(webhookId).isPresent()) {
            log.warn("Duplicate webhook ignored: {}", webhookId);
            return;
        }

        // 2. FIND OR CREATE PAYMENT
        Payment payment = paymentRepository.findByPledgeId(pledgeId)
                .orElse(Payment.builder()
                        .pledgeId(pledgeId)
                        .amount(amount)
                        .status("PENDING")
                        .build());

        // 3. STATE MACHINE CHECK (Fixes "Backward Overwrite")
        if (!payment.isValidTransition(newStatus)) {
            log.error("Invalid state transition attempted for Pledge {}: {} -> {}",
                    pledgeId, payment.getStatus(), newStatus);
            return; // Ignore invalid state updates (e.g. Authorized coming after Captured)
        }

        // 4. UPDATE STATE
        payment.setStatus(newStatus);
        payment.setWebhookId(webhookId);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 5. PUBLISH EVENT (For Campaign/Donation Services)
        publishPaymentEvent(payment);
        log.info("Payment processed successfully: {} -> {}", pledgeId, newStatus);
    }

    private void publishPaymentEvent(Payment payment) {
        Map<String, Object> event = Map.of(
                "pledgeId", payment.getPledgeId(),
                "status", payment.getStatus(),
                "amount", payment.getAmount()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
    }
}