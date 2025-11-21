package com.careforall.donation_service.worker;

import com.careforall.donation_service.config.RabbitMQConfig; // FIX: Import Config
import com.careforall.donation_service.dto.DonationEventDTO; // FIX: Import DTO
import com.careforall.donation_service.model.OutboxEvent;
import com.careforall.donation_service.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // FIX: Import Jackson
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // Runs every 5 seconds
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalse();

        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed events. Publishing...", events.size());

        for (OutboxEvent event : events) {
            try {
                // 1. Convert payload to DTO
                DonationEventDTO dto = objectMapper.convertValue(event.getPayload(), DonationEventDTO.class);

                // 2. Send to RabbitMQ
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        dto
                );

                // Mark as processed
                event.setProcessed(true);

            } catch (Exception e) {
                log.error("Failed to publish event {}", event.getId(), e);
                // Continue to next event so one failure doesn't block the batch
            }
        }

        // 3. Save all processed events
        outboxEventRepository.saveAll(events);
        log.info("Successfully published and marked {} events as processed.", events.size());
    }
}