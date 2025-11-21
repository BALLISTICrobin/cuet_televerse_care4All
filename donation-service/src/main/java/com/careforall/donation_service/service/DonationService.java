package com.careforall.donation_service.service;

import com.careforall.donation_service.model.Pledge;
import com.careforall.donation_service.model.OutboxEvent;
import com.careforall.donation_service.repository.DonationRepository;
import com.careforall.donation_service.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper; // Spring Boot provides this automatically

    @Transactional // ATOMIC TRANSACTION
    public Pledge createPledge(Pledge pledge) {
        // 1. Save the Pledge
        Pledge savedPledge = donationRepository.save(pledge);

        // 2. Save the Event to Outbox Table (Same Transaction)
        // We convert the DTO to a Map to store it as JSON in the DB
        Map<String, Object> payload = Map.of(
                "campaignId", savedPledge.getCampaignId(),
                "amount", savedPledge.getAmount(),
                "status", savedPledge.getStatus()
        );

        OutboxEvent event = OutboxEvent.builder()
                .eventType("DONATION_CREATED")
                .payload(payload)
                .processed(false)
                .build();

        outboxEventRepository.save(event);

        return savedPledge;
    }
}