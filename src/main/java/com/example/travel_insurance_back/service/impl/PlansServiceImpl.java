package com.example.travel_insurance_back.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.travel_insurance_back.dto.request.CancelRequestDto;
import com.example.travel_insurance_back.dto.response.PlansResponseDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto;
import com.example.travel_insurance_back.entity.Policy;
import com.example.travel_insurance_back.mapper.PolicyMapper;
import com.example.travel_insurance_back.service.PlansService;
import com.example.travel_insurance_back.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlansServiceImpl implements PlansService {

    private static final Set<String> CANCELLABLE_STATUSES = Set.of("DRAFT", "SIGNING");

    private final PolicyMapper policyMapper;
    private final PolicyService policyService;

    @Override
    public List<PlansResponseDto> findMyPlans(Long applicantId) {
        return policyMapper.findPoliciesByApplicantId(applicantId);
    }

    @Override
    public PolicyResponseDto findMyPlanDetail(Long policyId, Long applicantId) {
        PolicyResponseDto detail = policyService.findPolicyDetail(policyId);
        validateOwnership(detail.getPolicyId(), applicantId);
        return detail;
    }

    @Override
    public void cancel(CancelRequestDto request, Long applicantId) {
        Policy policy = policyMapper.selectById(request.getPolicyId());

        validatePolicyExists(policy);
        validateOwnership(policy, applicantId);
        validateCancellable(policy);

        policyMapper.update(
                new LambdaUpdateWrapper<Policy>()
                        .eq(Policy::getPolicyId, request.getPolicyId())
                        .set(Policy::getStatus, "VOID")
        );
    }

    // --- 驗證 ---

    private void validatePolicyExists(Policy policy) {
        if (policy == null)
            throw new IllegalArgumentException("查無保單");
    }

    private void validateOwnership(Policy policy, Long applicantId) {
        if (!policy.getApplicantId().equals(applicantId))
            throw new IllegalArgumentException("無權限操作此保單");
    }

    private void validateOwnership(Long policyId, Long applicantId) {
        Policy policy = policyMapper.selectById(policyId);
        validatePolicyExists(policy);
        validateOwnership(policy, applicantId);
    }

    private void validateCancellable(Policy policy) {
        if (!CANCELLABLE_STATUSES.contains(policy.getStatus()))
            throw new IllegalArgumentException(
                    "保單狀態為「" + policy.getStatus() + "」，無法取消");
    }
}