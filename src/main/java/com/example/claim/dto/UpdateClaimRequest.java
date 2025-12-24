package com.example.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Required for JSON deserialization
public class UpdateClaimRequest {

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Status is required")
    private String remarks; // Optional by default

    @PositiveOrZero(message = "Approved amount cannot be negative")
    private Integer approvedAmount;
}