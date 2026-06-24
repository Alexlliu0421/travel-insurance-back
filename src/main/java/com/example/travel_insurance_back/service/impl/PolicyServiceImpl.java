package com.example.travel_insurance_back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.travel_insurance_back.dto.request.ApplyRequestDto;
import com.example.travel_insurance_back.dto.request.QuoteRequestDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto;
import com.example.travel_insurance_back.dto.response.QuoteResponseDto;
import com.example.travel_insurance_back.entity.Policy;
import com.example.travel_insurance_back.mapper.PolicyMapper;
import com.example.travel_insurance_back.service.PolicyService;
import com.example.travel_insurance_back.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {


    private final PolicyMapper policyMapper;
    private final QuoteService quoteService;

    @Override
    public PolicyResponseDto apply(Long applicantId, ApplyRequestDto request) {
        validateInsuredInfo(request);

        QuoteRequestDto quoteRequest = QuoteRequestDto.builder()
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .insuredBirthDate(request.getInsuredBirthDate())
                .insuredGender(request.getInsuredGender())
                .insuredOccupationCode(request.getInsuredOccupationCode())
                .coverageId(request.getCoverageId())
                .build();

        QuoteResponseDto quote = quoteService.calculate(quoteRequest);

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .applicantId(applicantId)
                .insuredName(request.getInsuredName())
                .insuredIdNumber(request.getInsuredIdNumber())
                .insuredBirthDate(request.getInsuredBirthDate())
                .insuredGender(request.getInsuredGender())
                .insuredOccupationCode(request.getInsuredOccupationCode())
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .insuredDays(quote.getInsuredDays())
                .coverageId(request.getCoverageId())
                .basePremium(quote.getBasePremium())
                .finalPremium(quote.getFinalPremium())
                .status("DRAFT")
                .build();

        policyMapper.insert(policy);

        return findPolicyDetail(policy.getPolicyId());
    }

    @Override
    public PolicyResponseDto findPolicyDetail(Long policyId) {
        PolicyResponseDto detail = policyMapper.findPolicyDetailById(policyId);
        if (detail == null)
            throw new IllegalArgumentException("查無保單：" + policyId);
        return detail;
    }

    // --- 驗證 ---

    private void validateInsuredInfo(ApplyRequestDto request) {
        if (request.getInsuredName() == null || request.getInsuredName().isBlank())
            throw new IllegalArgumentException("被保人姓名不得為空");
        if (request.getInsuredIdNumber() == null || request.getInsuredIdNumber().isBlank())
            throw new IllegalArgumentException("被保人身份證號不得為空");
        if (request.getInsuredBirthDate() == null)
            throw new IllegalArgumentException("被保人生日不得為空");
        if (request.getInsuredBirthDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("被保人生日不得晚於今日");
        if (request.getInsuredGender() == null)
            throw new IllegalArgumentException("被保人性別不得為空");
    }

    // --- 工具 ---

    private String generatePolicyNumber() {
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefix = "POL-" + date + "-";

    Long countToday = policyMapper.selectCount(
            new LambdaQueryWrapper<Policy>()
                    .likeRight(Policy::getPolicyNumber, prefix)
    );

    return String.format("%s%04d", prefix, countToday + 1);
}
}