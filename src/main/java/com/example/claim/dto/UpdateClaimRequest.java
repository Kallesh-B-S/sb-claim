package com.example.claim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateClaimRequest {
    private String status;
    private String remarks;
}
