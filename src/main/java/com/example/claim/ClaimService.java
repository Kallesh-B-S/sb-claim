package com.example.claim;

import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.example.claim.dto.ClaimResponse;
import com.example.claim.dto.ClaimWithImageUrls;
import com.example.claim.dto.CreateClaimRequest;
import com.example.claim.dto.CreateUpdateResponse;
import com.example.claim.dto.Policy;
import com.example.claim.dto.UpdateClaimRequest;
import com.example.claim.exception.DataNotFoundException;

import jakarta.validation.Valid;

@Service
public class ClaimService {

    @Autowired
    ClaimDao claimDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestClient restClient;

    @Autowired
    private ClaimImageDao claimImageDao;

    @Transactional
    public CreateUpdateResponse createClaim(CreateClaimRequest requestBody, List<MultipartFile> images) {

        String customerUrl = "http://localhost:8766/customer/" + requestBody.getCustomerId();
        String policyUrl = "http://localhost:8766/customer/" + requestBody.getCustomerId();

        System.out.println("------------------ Customer URL start ----------------------");
        System.out.println(customerUrl);
        System.out.println("------------------ Customer URL end ----------------------");

        try {
            // We only care if it exists, so we try to fetch it
            restTemplate.getForObject(customerUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            // This triggers if the User Service returns a 404
            throw new DataNotFoundException("Validation Failed: Customer ID " +
                    requestBody.getCustomerId() + " does not exist.");
        }

        Claim claim = new Claim();
        claim.setPolicyId(requestBody.getPolicyId());
        claim.setCustomerId(requestBody.getCustomerId());
        claim.setStatus("SUBMITTED");
        claim.setRemarks("Claim Submitted");
        claim.setRequestedAmount(requestBody.getRequestedAmount());
        claim.setDescription(requestBody.getDescription());
        claim.setIncidentDate(requestBody.getIncidentDate());

        Claim savedClaim = claimDao.save(claim);

        String generatedClaimNumber = "CLM-" + savedClaim.getId();
        savedClaim.setClaimNumber(generatedClaimNumber);

        claimDao.save(savedClaim);

        // 3. Handle Images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                // Skip empty files if any
                if (file.isEmpty())
                    continue;

                String path = saveFileToDisk(file, savedClaim.getId());

                ClaimImage img = new ClaimImage();
                img.setFilePath(path);
                img.setClaimId(savedClaim.getId());
                claimImageDao.save(img);
            }
        }

        return new CreateUpdateResponse(savedClaim.getId(), "Claim Created Successfully");
    }

    private String saveFileToDisk(MultipartFile file, Integer claimId) {
        try {
            String uploadDir = "uploads/claims/" + claimId + "/";
            File dir = new File(uploadDir);
            if (!dir.exists())
                dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    // Remove "throws Exception"
    public CreateUpdateResponse addImagesToClaim(List<MultipartFile> images, int clientId) {
        try {
            if (images == null || images.isEmpty()) {
                // This is a RuntimeException, it doesn't need to be declared in the signature
                throw new BadRequestException("No Image found to add");
            }

            System.out.println("--------------- : "+images.size());

            for (MultipartFile file : images) {
                if (file.isEmpty()){
                    throw new BadRequestException("No Image found to add");
                    // continue;
                }

                String path = saveFileToDisk(file, clientId);

                ClaimImage img = new ClaimImage();
                img.setFilePath(path);
                img.setClaimId(clientId);
                claimImageDao.save(img);
            }
        } catch (com.example.claim.exception.BadRequestException e) {
            // Re-throw your custom exception so the @ExceptionHandler catches it
            throw e;
        } catch (Exception e) {
            // Wrap generic exceptions in a RuntimeException or handle them
            throw new RuntimeException("Error saving files: " + e.getMessage());
        }

        return new CreateUpdateResponse(null, "Images Added Successfully!");
    }

    // public List<Claim> getAllClaims() {
    // return claimDao.findAll();
    // }

    public List<ClaimResponse> getAllClaimsWithPolicy() {

        List<Claim> claims = claimDao.findAll();

        return claims.stream().map(claim -> {
            ClaimResponse response = new ClaimResponse();
            // Map basic fields
            response.setClaimId(claim.getId());
            response.setPolicyId(claim.getPolicyId());
            response.setCustomerId(claim.getCustomerId());
            response.setClaimNumber(claim.getClaimNumber());
            response.setStatus(claim.getStatus());
            response.setRemarks(claim.getRemarks());

            // Fetch Policy using RestClient
            try {
                Policy policy = restClient.get()
                        .uri("http://localhost:8096/policy/{id}", claim.getPolicyId())
                        .retrieve()
                        .body(Policy.class);
                response.setPolicy(policy);
            } catch (Exception e) {
                // If policy service is down, the policy field remains null
                System.err.println("Could not fetch policy " + claim.getPolicyId());
            }

            return response;
        }).collect(Collectors.toList());

    }

    public List<ClaimResponse> getAllClaimsBatch() {
        // 1. Fetch all claims
        List<Claim> claims = claimDao.findAll();
        if (claims.isEmpty())
            return Collections.emptyList();

        // 2. Collect all unique Policy IDs
        Set<Integer> policyIds = claims.stream()
                .map(Claim::getPolicyId)
                .collect(Collectors.toSet());

        // 3. Single Batch Call to Policy Service
        Map<Integer, Policy> policyMap = new HashMap<>();
        try {
            // We expect a List<Policy> back
            List<Policy> policies = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost")
                            .port(8096)
                            .path("/policy/batch/list")
                            .queryParam("ids", policyIds)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Policy>>() {
                    });

            // Convert list to Map for fast lookup: Map<id, Policy>
            if (policies != null) {
                policyMap = policies.stream()
                        .collect(Collectors.toMap(Policy::getId, p -> p, (p1, p2) -> p1));
            }
        } catch (Exception e) {
            System.err.println("Batch policy fetch failed: " + e.getMessage());
        }

        // System.out.println(policyIds);
        // System.out.println(policyMap);

        // 4. Map the data back to ClaimResponse
        Map<Integer, Policy> finalPolicyMap = policyMap; // for lambda
        return claims.stream().map(claim -> {
            ClaimResponse response = new ClaimResponse();
            response.setClaimId(claim.getId());
            response.setPolicyId(claim.getPolicyId());
            response.setCustomerId(claim.getCustomerId());
            response.setClaimNumber(claim.getClaimNumber());
            response.setStatus(claim.getStatus());
            response.setRemarks(claim.getRemarks());
            response.setRequestedAmount(claim.getRequestedAmount());
            response.setDescription(claim.getDescription());

            // Get from the map (O(1) lookup) instead of making a network call
            response.setPolicy(finalPolicyMap.get(claim.getPolicyId()));

            return response;
        }).collect(Collectors.toList());
    }

    public ClaimWithImageUrls getClaimById(int claimId) {
        Claim claim = claimDao.findById(claimId).orElseThrow(() -> new DataNotFoundException("Invalid Claim Id"));
        List<ClaimImage> images = claimImageDao.findByClaimId(claim.getId());
        ClaimWithImageUrls claimWithImageUrls = new ClaimWithImageUrls();
        claimWithImageUrls.setId(claim.getId());
        claimWithImageUrls.setPolicyId(claim.getPolicyId());
        claimWithImageUrls.setCustomerId(claim.getCustomerId());
        claimWithImageUrls.setClaimNumber(claim.getClaimNumber());
        claimWithImageUrls.setRequestedAmount(claim.getRequestedAmount());
        claimWithImageUrls.setApprovedAmount(claim.getApprovedAmount());
        claimWithImageUrls.setDescription(claim.getDescription());
        claimWithImageUrls.setIncidentDate(claim.getIncidentDate());
        claimWithImageUrls.setStatus(claim.getStatus());
        claimWithImageUrls.setRemarks(claim.getRemarks());

        // List<String> filePaths = (images != null && !images.isEmpty())
        // ? images.stream().map(ClaimImage::getFilePath).toList()
        // : Collections.emptyList();

        List<String> filePaths = (images != null && !images.isEmpty())
                ? images.stream()
                        .map(img -> img.getFilePath().replace("\\", "/"))
                        .toList()
                : Collections.emptyList();

        claimWithImageUrls.setImages(filePaths);

        return claimWithImageUrls;
    }

    public CreateUpdateResponse updateClaimStatus(int claimId, UpdateClaimRequest requestBody) {
        Claim existingClaim = claimDao.findById(claimId)
                .orElseThrow(() -> new DataNotFoundException("Invalid Claim ID"));
        existingClaim.setStatus(requestBody.getStatus());
        existingClaim.setRemarks(requestBody.getRemarks());

        // Check if approvedAmount is provided in the request
        if (requestBody.getApprovedAmount() != null) {
            existingClaim.setApprovedAmount(requestBody.getApprovedAmount());
        }

        Claim updatedClaim = claimDao.save(existingClaim);
        return new CreateUpdateResponse(updatedClaim.getId(), "Claim Status Updated Successfully!");
    }

    public List<Claim> getClaimByPolicyId(Integer policyId) {
        return claimDao.findByPolicyId(policyId);
    }

    public List<Claim> getClaimBycustomerId(Integer customerId) {
        return claimDao.findByCustomerId(customerId);
    }

    public List<Claim> getAllClaims() {
        return claimDao.findAll();
    }

}
