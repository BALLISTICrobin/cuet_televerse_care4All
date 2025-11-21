package com.careforall.donation_service.repository;

import com.careforall.donation_service.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    // Find events that haven't been sent yet
    List<OutboxEvent> findByProcessedFalse();
}