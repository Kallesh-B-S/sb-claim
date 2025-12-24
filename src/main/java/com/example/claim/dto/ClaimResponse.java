package com.example.claim.dto;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClaimResponse {

    private Integer claimId;

    private Integer policyId;

    private Integer customerId;

    private String claimNumber;

    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;

    private String description;

    private String status;

    private String remarks;

    private Policy policy;
}
