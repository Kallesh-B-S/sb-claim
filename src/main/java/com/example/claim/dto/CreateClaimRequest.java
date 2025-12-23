package com.example.claim.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateClaimRequest {

    @NotNull(message = "Policy ID is required")
    private Integer policyId;

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "Requested amount is required")
    @Positive(message = "Requested amount must be greater than zero")
    private Integer requestedAmount;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate incidentDate;

    // private String status;
    // private String remarks;
}