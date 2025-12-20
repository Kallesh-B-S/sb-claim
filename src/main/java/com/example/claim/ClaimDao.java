package com.example.claim;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimDao extends JpaRepository<Claim, Integer> {

    // Spring generates: SELECT * FROM claim WHERE policy_id = ?
    List<Claim> findByPolicyId(Integer policyId);

    // Spring generates: SELECT * FROM claim WHERE customer_id = ?
    List<Claim> findByCustomerId(Integer customerId);
    
}
