package com.careforall.campaign_service.event;

import lombok.Data;

import java.math.BigDecimal;

// DTO Class (Keep inside the same file or separate package)
@Data
class DonationEventDTO {
    private String campaignId;
    private BigDecimal amount;
    private String status;
}
