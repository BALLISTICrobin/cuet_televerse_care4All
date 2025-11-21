package com.careforall.campaign_service.controller;

import com.careforall.campaign_service.model.Campaign;
import com.careforall.campaign_service.model.CampaignTotal;
import com.careforall.campaign_service.repository.CampaignRepository;
import com.careforall.campaign_service.repository.CampaignTotalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignRepository campaignRepository;
    private final CampaignTotalRepository campaignTotalRepository;

    // 1. Create Campaign (Admin/Organizer)
    @PostMapping
    public ResponseEntity<Campaign> createCampaign(@RequestBody Campaign campaign) {
        // Force status to active on creation
        campaign.setStatus("active");
        Campaign savedCampaign = campaignRepository.save(campaign);
        return ResponseEntity.ok(savedCampaign);
    }

    // 2. Get Single Campaign (Merges Details + Live Stats)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCampaign(@PathVariable String id) {
        Optional<Campaign> campaignOpt = campaignRepository.findById(id);

        if (campaignOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Fetch the dynamic totals (or return default 0 if no donations yet)
        CampaignTotal stats = campaignTotalRepository.findById(id)
                .orElse(CampaignTotal.builder()
                        .campaignId(id)
                        .totalPledged(BigDecimal.ZERO)
                        .totalCaptured(BigDecimal.ZERO)
                        .donationCount(0)
                        .build());

        // Merge into a single JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("details", campaignOpt.get());
        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }

    // 3. Get All Campaigns
    @GetMapping
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        return ResponseEntity.ok(campaignRepository.findAll());
    }
}