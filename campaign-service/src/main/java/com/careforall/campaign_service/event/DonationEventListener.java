package com.careforall.campaign_service.event;

import com.careforall.campaign_service.config.RabbitMQConfig;
import com.careforall.campaign_service.model.CampaignTotal;
import com.careforall.campaign_service.repository.CampaignTotalRepository;
import com.careforall.campaign_service.event.DonationEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationEventListener {

    private final CampaignTotalRepository campaignTotalRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    @Transactional
    public void handleDonationEvent(DonationEventDTO event) {
        log.info("Received donation event: {}", event);

        // Upsert Logic: Get existing total OR create a new record if it's the first donation
        CampaignTotal total = campaignTotalRepository.findById(event.getCampaignId())
                .orElse(CampaignTotal.builder()
                        .campaignId(event.getCampaignId())
                        .totalPledged(BigDecimal.ZERO)
                        .totalCaptured(BigDecimal.ZERO)
                        .donationCount(0)
                        .build());

        // Update the totals
        // NOTE: We only update 'pledged' here. 'captured' would be updated by a separate Payment Success event.
        total.setTotalPledged(total.getTotalPledged().add(event.getAmount()));
        total.setDonationCount(total.getDonationCount() + 1);
        total.setUpdatedAt(LocalDateTime.now());

        campaignTotalRepository.save(total);
        log.info("Updated totals for campaign {}: New Total Pledged = {}",
                event.getCampaignId(), total.getTotalPledged());
    }
}

