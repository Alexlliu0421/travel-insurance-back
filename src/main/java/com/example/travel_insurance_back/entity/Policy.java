package com.example.travel_insurance_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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
@TableName("policies")
public class Policy {

    @TableId(type = IdType.AUTO)
    private Long policyId;

    private String policyNumber;

    // 要保人
    private Long applicantId;

    // 被保人
    private String insuredName;

    private String insuredIdNumber;

    private LocalDate insuredBirthDate;

    private Integer insuredGender;  // 0:女 1:男

    private String insuredOccupationCode;

    // 旅遊資訊
    private LocalDate departureDate;

    private LocalDate returnDate;

    private Integer insuredDays;

    // 保額
    private Long coverageId;

    // 保費
    private BigDecimal basePremium;

    private BigDecimal finalPremium;

    private String status;  // Draft / Signing / Finish / Rejected

    private Long agentId;

    private Long reviewerId;

    private LocalDateTime lastReviewDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}