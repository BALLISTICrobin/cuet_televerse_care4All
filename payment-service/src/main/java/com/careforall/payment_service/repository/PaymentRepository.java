package com.careforall.payment_service.repository;

import com.careforall.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByWebhookId(String webhookId);
    Optional<Payment> findByPledgeId(String pledgeId);
}