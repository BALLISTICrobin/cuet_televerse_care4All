package com.careforall.donation_service.controller;

import com.careforall.donation_service.model.Pledge;
import com.careforall.donation_service.service.DonationService; // Import the Service
import com.careforall.donation_service.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    // FIX 1: Inject the Service (This was missing)
    private final DonationService donationService;

    // FIX 2: Inject Idempotency Service
    private final IdempotencyService idempotencyService;

    // Note: We removed DonationRepository because DonationService now handles the DB save.

    @PostMapping
    public ResponseEntity<?> createDonation(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> payload) {

        // 1. CHECK IDEMPOTENCY
        if (!idempotencyService.process(idempotencyKey)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate Request Detected");
        }

        try {
            // 2. BUILD PLEDGE OBJECT
            Pledge donation = Pledge.builder()
                    .userId((String) payload.get("userId"))
                    .campaignId((String) payload.get("campaignId"))
                    .amount(new BigDecimal(payload.get("amount").toString()))
                    .idempotencyKey(idempotencyKey)
                    .status("PENDING") // or "initiated"
                    .createdAt(LocalDateTime.now())
                    .build();

            // 3. CALL SERVICE (Now this works because the field exists)
            Pledge savedPledge = donationService.createPledge(donation);

            return ResponseEntity.ok(savedPledge);

        } catch (Exception e) {
            e.printStackTrace(); // Good for debugging during hackathon
            return ResponseEntity.internalServerError().build();
        }
    }
}