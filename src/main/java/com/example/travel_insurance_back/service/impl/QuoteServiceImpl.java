package com.example.travel_insurance_back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.travel_insurance_back.dto.request.QuoteRequestDto;
import com.example.travel_insurance_back.dto.response.QuoteResponseDto;
import com.example.travel_insurance_back.entity.CoverageAmount;
import com.example.travel_insurance_back.entity.MortalityRate;
import com.example.travel_insurance_back.entity.OccupationRate;
import com.example.travel_insurance_back.mapper.CoverageAmountMapper;
import com.example.travel_insurance_back.mapper.MortalityRateMapper;
import com.example.travel_insurance_back.mapper.OccupationRateMapper;
import com.example.travel_insurance_back.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private static final BigDecimal BASE_DAILY_RATE = new BigDecimal("0.0002");

    private final MortalityRateMapper mortalityRateMapper;
    private final OccupationRateMapper occupationRateMapper;
    private final CoverageAmountMapper coverageAmountMapper;

    @Override
    public QuoteResponseDto calculate(QuoteRequestDto request) {
        validateDateRange(request.getDepartureDate(), request.getReturnDate());

        int insuredDays = calculateInsuredDays(
                request.getDepartureDate(), request.getReturnDate());
        validateInsuredDays(insuredDays);

        int age = calculateAge(request.getInsuredBirthDate());

        MortalityRate mortalityRate = findMortalityRate(age, request.getInsuredGender());
        OccupationRate occupationRate = findOccupationRate(request.getInsuredOccupationCode());
        CoverageAmount coverage = findActiveCoverage(request.getCoverageId());

        BigDecimal basePremium = calculateBasePremium(coverage.getCoverageAmount(), insuredDays);
        BigDecimal finalPremium = calculateFinalPremium(
                basePremium,
                mortalityRate.getRateMultiplier(),
                occupationRate.getRateMultiplier(),
                coverage.getRateMultiplier()
        );

        return QuoteResponseDto.builder()
                .coverageId(coverage.getCoverageId())
                .coverageAmount(coverage.getCoverageAmount())
                .insuredDays(insuredDays)
                .basePremium(basePremium)
                .finalPremium(finalPremium)
                .build();
    }

    // --- 驗證 ---

    private void validateDateRange(LocalDate departure, LocalDate returnDate) {
        if (departure == null || returnDate == null)
            throw new IllegalArgumentException("出發日與回程日不得為空");
        if (!departure.isBefore(returnDate))
            throw new IllegalArgumentException("出發日必須早於回程日");
        if (departure.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("出發日不得早於今日");
    }

    private void validateInsuredDays(int days) {
        if (days < 3 || days > 180)
            throw new IllegalArgumentException("投保天數須介於 3 至 180 天");
    }

    // --- 查費率表 ---

    private MortalityRate findMortalityRate(int age, int gender) {
        MortalityRate rate = mortalityRateMapper.selectOne(
                new LambdaQueryWrapper<MortalityRate>()
                        .eq(MortalityRate::getAge, age)
                        .eq(MortalityRate::getGender, gender)
        );
        if (rate == null)
            throw new IllegalArgumentException("查無對應年齡性別之死亡率資料");
        return rate;
    }

    private OccupationRate findOccupationRate(String occupationCode) {
        OccupationRate rate = occupationRateMapper.selectById(occupationCode);
        if (rate == null)
            throw new IllegalArgumentException("查無對應職業費率資料：" + occupationCode);
        return rate;
    }

    private CoverageAmount findActiveCoverage(Long coverageId) {
        CoverageAmount coverage = coverageAmountMapper.selectOne(
                new LambdaQueryWrapper<CoverageAmount>()
                        .eq(CoverageAmount::getCoverageId, coverageId)
                        .eq(CoverageAmount::getIsActive, 1)
        );
        if (coverage == null)
            throw new IllegalArgumentException("查無有效保額方案");
        return coverage;
    }

    // --- 計算 ---

    private int calculateInsuredDays(LocalDate departure, LocalDate returnDate) {
        return (int) departure.datesUntil(returnDate.plusDays(1)).count();
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private BigDecimal calculateBasePremium(BigDecimal coverageAmount, int insuredDays) {
        return coverageAmount
                .multiply(BASE_DAILY_RATE)
                .multiply(BigDecimal.valueOf(insuredDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalPremium(BigDecimal basePremium,
                                              BigDecimal mortalityMultiplier,
                                              BigDecimal occupationMultiplier,
                                              BigDecimal coverageMultiplier) {
        return basePremium
                .multiply(mortalityMultiplier)
                .multiply(occupationMultiplier)
                .multiply(coverageMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }
}