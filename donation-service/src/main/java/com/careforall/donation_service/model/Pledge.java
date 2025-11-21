package com.careforall.donation_service.model;

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
@Table(name = "pledges") // Matches SQL Schema
public class Pledge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "campaign_id")
    private String campaignId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "donor_email")
    private String donorEmail;

    @Column(nullable = false)
    private BigDecimal amount;

    // Status: initiated | authorized | captured | failed | refunded
    @Builder.Default
    private String status = "initiated";

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    // CRITICAL: Implements Optimistic Locking from Schema
    @Version
    @Column(nullable = false) // Default 1 in DB
    private Integer version;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}