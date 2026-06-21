package com.example.travel_insurance_back.service;

import com.example.travel_insurance_back.dto.request.ApplyRequestDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto;

public interface PolicyService {
    PolicyResponseDto apply(Long applicantId, ApplyRequestDto request);
    PolicyResponseDto findPolicyDetail(Long policyId);
}