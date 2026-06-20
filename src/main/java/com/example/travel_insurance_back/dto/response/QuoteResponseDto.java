package com.example.travel_insurance_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDto {
    private Long coverageId;
    private BigDecimal coverageAmount;    // 保額
    private Integer insuredDays;          // 投保天數
    private BigDecimal basePremium;       // 基本保費
    private BigDecimal finalPremium;      // 實際保費
}