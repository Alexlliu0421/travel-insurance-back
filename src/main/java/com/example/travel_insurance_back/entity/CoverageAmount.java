package com.example.travel_insurance_back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coverage_amounts")
public class CoverageAmount {

    @TableId(type = IdType.AUTO)
    private Long coverageId;

    private BigDecimal coverageAmount;

    private BigDecimal rateMultiplier;

    private Integer isActive;  // 0:停用 1:啟用
}