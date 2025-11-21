package com.careforall.campaign_service.repository;

import com.careforall.campaign_service.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, String> {
    // Standard CRUD operations are automatically provided by JpaRepository
    // (save, findById, findAll, delete, etc.)
}