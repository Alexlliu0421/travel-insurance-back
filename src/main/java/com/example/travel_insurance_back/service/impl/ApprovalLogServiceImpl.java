package com.example.travel_insurance_back.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_insurance_back.dto.ApprovalRequest;
import com.example.travel_insurance_back.entity.ApprovalLog;
import com.example.travel_insurance_back.mapper.ApprovalLogMapper;
import com.example.travel_insurance_back.service.ApprovalLogService;

@Service
public class ApprovalLogServiceImpl implements ApprovalLogService {

    @Autowired
    private ApprovalLogMapper approvalLogMapper;

    @Override
    @Transactional
    public void addLog(ApprovalRequest request) {
        // 1. 權限與邏輯決策
        String validAction = validateAndGetAction(request.getRole(), request.getAction());
        
        // 2. 轉換為 Entity
        ApprovalLog log = new ApprovalLog();
        log.setPolicyId(request.getPolicyId());
        log.setOperatorId(request.getUserId());
        log.setAction(validAction); // 確保存入的是經過校驗的 Action
        log.setRemark(request.getRemark());
        log.setCreatedDate(LocalDateTime.now());
        
        approvalLogMapper.insert(log);
    }

    // 私有方法：負責封裝權限與規則
    private String validateAndGetAction(String role, String action) {
        if ("AGENT".equals(role)) {
            if (!"SUBMIT".equals(action)) {
                throw new IllegalArgumentException("業務員只能執行送審動作");
            }
        } else if ("MANAGER".equals(role)) {
            if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
                throw new IllegalArgumentException("主管只能執行核准或駁回動作");
            }
        } else {
            throw new IllegalArgumentException("未知角色，拒絕存取");
        }
        return action; // 驗證通過
    }
    
}
