package com.careforall.campaign_service.repository;

import com.careforall.campaign_service.model.CampaignTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignTotalRepository extends JpaRepository<CampaignTotal, String> {
}