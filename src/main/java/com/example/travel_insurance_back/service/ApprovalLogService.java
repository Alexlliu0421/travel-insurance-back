package com.example.travel_insurance_back.service;

import java.util.List;
import java.util.Map;

import com.example.travel_insurance_back.dto.ApprovalRequest;
import com.example.travel_insurance_back.entity.ApprovalLog;

public interface ApprovalLogService {

	void addLog(ApprovalRequest approvalRequest);
	List<ApprovalLog> getLogsByPolicyId(Long policyId, Long userId, String role);
	List<Map<String, Object>> getPolicyList(Long userId, String role);
	List<Map<String, Object>> getWorklist(Long userId, String role);
	Map<String, Object> getPolicyById(Long policyId);
}
