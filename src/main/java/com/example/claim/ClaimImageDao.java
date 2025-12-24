package com.example.claim;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimImageDao extends JpaRepository<ClaimImage, Integer> {

    List<ClaimImage> findByClaimId(Integer id);
    
}
