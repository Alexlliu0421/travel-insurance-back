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
public class PlansResponseDto {
    private Long policyId;
    private String policyNumber;
    private String insuredName;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Integer insuredDays;
    private BigDecimal coverageAmount;
    private BigDecimal finalPremium;
    private String status;
    private LocalDateTime createdAt;
}