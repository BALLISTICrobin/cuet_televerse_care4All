package com.careforall.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "pledge_id", nullable = false)
    private String pledgeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // AUTHORIZED, CAPTURED, FAILED, REFUNDED

    // Stores the external provider's ID to prevent processing the same webhook twice
    @Column(name = "webhook_id", unique = true)
    private String webhookId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- STATE MACHINE LOGIC ---
    public boolean isValidTransition(String newStatus) {
        // If already finalized, cannot change
        if ("CAPTURED".equals(this.status) || "FAILED".equals(this.status)) {
            // Only allow REFUNDED from CAPTURED
            return "REFUNDED".equals(newStatus) && "CAPTURED".equals(this.status);
        }

        // If currently AUTHORIZED, can move to CAPTURED or FAILED
        if ("AUTHORIZED".equals(this.status)) {
            return "CAPTURED".equals(newStatus) || "FAILED".equals(newStatus);
        }

        // Initial state is usually null or PENDING
        return true;
    }
}