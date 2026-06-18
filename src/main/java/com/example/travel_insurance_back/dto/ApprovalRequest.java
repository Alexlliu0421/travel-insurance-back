package com.example.travel_insurance_back.dto;

import lombok.Data;

/**
 * 專門負責接收「簽核」請求的資料傳輸物件
 */
@Data
public class ApprovalRequest {
    private Long policyId;   // 前端傳來的保單ID
    private String action;   // SUBMIT, APPROVE, REJECT
    private String remark;   // 審核意見
    private Integer userId; // 從 X-User-Id 填入
    private String role;    // 從 X-Role 填入
	public Long getPolicyId() {
		return policyId;
	}
	public void setPolicyId(Long policyId) {
		this.policyId = policyId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
    
    
}