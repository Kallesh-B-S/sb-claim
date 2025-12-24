package com.example.claim;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.claim.dto.ClaimResponse;
import com.example.claim.dto.ClaimWithImageUrls;
import com.example.claim.dto.CreateClaimRequest;
import com.example.claim.dto.CreateUpdateResponse;
import com.example.claim.dto.UpdateClaimRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("claim")
public class ClaimController {

    @Autowired
    ClaimService claimService;

    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateUpdateResponse> createClaim(
            @RequestPart("requestBody") CreateClaimRequest requestBody,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        CreateUpdateResponse response = claimService.createClaim(requestBody, images);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // @GetMapping
    // public ResponseEntity<List<Claim>> getAllClaims() {
    // List<Claim> response = claimService.getAllClaims();
    // return ResponseEntity.ok(response);
    // }

    @GetMapping("allClaimsWithPolicy")
    public ResponseEntity<List<ClaimResponse>> getAllClaimsWithPolicy() {
        // List<ClaimResponse> response = claimService.getAllClaims();
        List<ClaimResponse> response = claimService.getAllClaimsBatch();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims() {
        List<Claim> response = claimService.getAllClaims();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<ClaimWithImageUrls> getClaimById(@Valid @PathVariable("id") Integer claimId) {
        ClaimWithImageUrls response = claimService.getClaimById(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policy/{id}")
    public ResponseEntity<List<Claim>> getClaimByPolicyId(@Valid @PathVariable("id") Integer policyId) {
        List<Claim> response = claimService.getClaimByPolicyId(policyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<List<Claim>> getClaimBycustomerId(@Valid @PathVariable("id") Integer customerId) {
        List<Claim> response = claimService.getClaimBycustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<CreateUpdateResponse> updateClaimStatus(@Valid @PathVariable("id") Integer claimId,
            @RequestBody UpdateClaimRequest requestBody) {
        CreateUpdateResponse response = claimService.updateClaimStatus(claimId, requestBody);
        return ResponseEntity.ok(response);
    }
}
