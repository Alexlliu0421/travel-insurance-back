package com.example.travel_insurance_back.service;

import com.example.travel_insurance_back.dto.request.CancelRequestDto;
import com.example.travel_insurance_back.dto.response.PlansResponseDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto;

import java.util.List;

public interface PlansService {
    List<PlansResponseDto> findMyPlans(Long applicantId);
    PolicyResponseDto findMyPlanDetail(Long policyId, Long applicantId);
    void cancel(CancelRequestDto request, Long applicantId);
}