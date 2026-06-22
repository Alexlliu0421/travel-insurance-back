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
    private Long userId; 
    private String role;    
	
    
}