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
@TableName("occupation_rates")
public class OccupationRate {

    @TableId(type = IdType.INPUT)
    private String occupationCode;

    private String occupationName;

    private BigDecimal rateMultiplier;
}