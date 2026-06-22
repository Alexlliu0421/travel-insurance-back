package com.example.travel_insurance_back.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.travel_insurance_back.entity.ApprovalLog;

@Mapper
public interface ApprovalLogMapper extends BaseMapper<ApprovalLog> {
	// 1. 查詢保單清單根據權限過濾
    List<Map<String, Object>> findPoliciesByRole(@Param("userId") Long userId, @Param("role") String role);
    // 2. 查詢特定保單的歷程記錄
    List<ApprovalLog> findLogsByPolicyId(@Param("policyId") Long policyId);
    // 3. 根據權限查詢工作區待處理保單
    List<Map<String, Object>> findWorklistByRole(@Param("userId") Long userId, @Param("role") String role);
	int updatePolicyForSubmit(Long policyId, Long userId, String nextStatus);
	int updatePolicyForManager(Long policyId, Long userId, String nextStatus);
	Map<String, Object> findPolicyById(Long policyId);
	String findUserEmailByPolicyId(Long policyId);
	String findPolicyNumberById(Long policyId);
	String findAgentEmailByPolicyId(Long policyId);
}
   