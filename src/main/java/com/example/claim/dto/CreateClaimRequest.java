package com.example.claim.dto;

import lombok.Data;

@Data
public class CreateClaimRequest {
    private Integer policyId;
    private Integer customerId;
    private String status;
    private String remarks;
}
