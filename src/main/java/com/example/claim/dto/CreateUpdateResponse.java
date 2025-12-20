package com.example.claim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUpdateResponse {
    private Integer id;
    private String message;
}
