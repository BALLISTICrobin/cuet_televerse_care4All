package com.careforall.campaign_service.model;

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
@Table(name = "campaign_totals")
public class CampaignTotal {

    @Id
    @Column(name = "campaign_id")
    private String campaignId; // Serves as PK and FK

    @Column(name = "total_pledged")
    @Builder.Default
    private BigDecimal totalPledged = BigDecimal.ZERO;

    @Column(name = "total_captured")
    @Builder.Default
    private BigDecimal totalCaptured = BigDecimal.ZERO;

    @Column(name = "donation_count")
    @Builder.Default
    private Integer donationCount = 0;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}