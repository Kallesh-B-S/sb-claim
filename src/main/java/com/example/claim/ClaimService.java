package com.example.claim;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.claim.dto.CreateClaimRequest;
import com.example.claim.dto.CreateUpdateResponse;
import com.example.claim.dto.UpdateClaimRequest;
import com.example.claim.exception.DataNotFoundException;

import jakarta.validation.Valid;

@Service
public class ClaimService {

    @Autowired
    ClaimDao claimDao;

    @Autowired
    private RestTemplate restTemplate;

    public CreateUpdateResponse createClaim(CreateClaimRequest requestBody) {

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
        claim.setStatus(requestBody.getStatus());
        claim.setRemarks(requestBody.getRemarks());
        Claim savedClaim = claimDao.save(claim);
        return new CreateUpdateResponse(savedClaim.getId(), "Claim Created Successfully");
    }

    public List<Claim> getAllClaims() {
        return claimDao.findAll();
    }

    public Claim getClaimById(int claimId) {
        return claimDao.findById(claimId).orElseThrow(() -> new DataNotFoundException("Invalid Claim Id"));
    }

    public CreateUpdateResponse updateClaimStatus(int claimId, UpdateClaimRequest requestBody) {
        Claim existingClaim = claimDao.findById(claimId)
                .orElseThrow(() -> new DataNotFoundException("Invalid Claim ID"));
        existingClaim.setStatus(requestBody.getStatus());
        existingClaim.setRemarks(requestBody.getRemarks());
        Claim updatedClaim = claimDao.save(existingClaim);
        return new CreateUpdateResponse(updatedClaim.getId(), "Claim Status Updated Successfully!");
    }

    public List<Claim> getClaimByPolicyId(Integer policyId) {
        return claimDao.findByPolicyId(policyId);
    }

    public List<Claim> getClaimBycustomerId(Integer customerId) {
        return claimDao.findByCustomerId(customerId);
    }

}
