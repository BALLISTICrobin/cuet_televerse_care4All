package com.careforall.campaign_service.event;

import com.careforall.campaign_service.config.RabbitMQConfig;
import com.careforall.campaign_service.model.CampaignTotal;
import com.careforall.campaign_service.repository.CampaignTotalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final CampaignTotalRepository campaignTotalRepository;

    // Listens to the Payment Exchange (You might need to bind a new queue in RabbitMQConfig for this)
    @RabbitListener(queues = "payment.queue") // Make sure to define this queue in Config!
    @Transactional
    public void handlePaymentEvent(Map<String, Object> event) {
        String status = (String) event.get("status");
        String pledgeId = (String) event.get("pledgeId");
        // Note: In a real app, we'd lookup CampaignID via PledgeID.
        // For this hackathon, we assume the event includes campaignId or we lookup.

        if ("CAPTURED".equals(status)) {
            log.info("Payment Captured for pledge: {}", pledgeId);
            // Logic to update totalCaptured would go here
            // (Requires linking Pledge -> Campaign, which we skipped for speed,
            // but you can add campaignId to the Payment/Webhook payload to fix easily)
        }
    }
}