package com.example.claim.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ClaimWithImageUrls {
    private Integer id;
    private Integer policyId;
    private Integer customerId;
    private String claimNumber;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private String description;
    private LocalDate incidentDate;
    private String status;
    private String remarks;
    private List<String> images;
}
