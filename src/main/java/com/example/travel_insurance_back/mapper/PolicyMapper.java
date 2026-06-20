package com.example.travel_insurance_back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.travel_insurance_back.entity.Policy;
import com.example.travel_insurance_back.dto.response.PlansResponseDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto ;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {

    // 查詢保單列表（含保額資料）
    List<PlansResponseDto> findPoliciesByApplicantId(@Param("applicantId") Long applicantId);

    // 查詢單筆保單詳情
    PolicyResponseDto  findPolicyDetailById(@Param("policyId") Long policyId);
}