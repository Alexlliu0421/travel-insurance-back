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
@TableName("mortality_rates")
public class MortalityRate {

    @TableId(type = IdType.AUTO)
    private Long mortalityId;

    private Integer age;

    private Integer gender;  // 0:女 1:男

    private BigDecimal deathRate;

    private BigDecimal rateMultiplier;
}