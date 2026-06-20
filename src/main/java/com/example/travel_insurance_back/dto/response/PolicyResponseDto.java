package com.example.travel_insurance_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponseDto {
    private Long policyId;
    private String policyNumber;

    // 被保人
    private String insuredName;
    private String insuredIdNumber;
    private LocalDate insuredBirthDate;
    private Integer insuredGender;
    private String insuredOccupationCode;
    private String occupationName;

    // 旅遊資訊
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer insuredDays;

    // 保額與保費
    private BigDecimal coverageAmount;
    private BigDecimal basePremium;
    private BigDecimal finalPremium;

    // 狀態
    private String status;
    private LocalDateTime lastReviewDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}