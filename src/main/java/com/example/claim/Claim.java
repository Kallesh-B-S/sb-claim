package com.example.claim;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claimId")
    private Integer id;

    // Link to your Policy Entity (Foreign Key from the Policy Service)
    @Column(nullable = false)
    private Integer policyId;

    @Column(nullable = false)
    private Integer customerId;

    private String claimNumber;

    private Integer requestedAmount;
    private Integer approvedAmount;

    private String description;

    // @Column(unique = true, nullable = false)
    // private String claimNumber; // e.g., CLM-100234

    // e.g., MEDICAL, ACCIDENT, THEFT
    // private String claimType;
    // private String description;

    // private BigDecimal claimAmount;
    // private BigDecimal approvedAmount;

    private LocalDate incidentDate;
    // private LocalDate filedDate;

    // SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PAID
    private String status;

    // Notes from the adjuster
    private String remarks;
}
