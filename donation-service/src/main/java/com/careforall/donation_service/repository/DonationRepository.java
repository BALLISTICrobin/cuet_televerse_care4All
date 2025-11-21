package com.careforall.donation_service.repository;

import com.careforall.donation_service.model.Pledge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Pledge, String> {
}