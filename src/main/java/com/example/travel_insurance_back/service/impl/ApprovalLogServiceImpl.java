package com.example.travel_insurance_back.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        int updatedRows = 0;
        String nextStatus = "";

        if ("SALESMAN".equals(request.getRole()) && "SUBMIT".equals(request.getAction())) {
            // 業務員送審：檢查是否為 DRAFT，且尚未被領取
            System.out.println("準備更新 Policy: ID=" + request.getPolicyId() + ", Status=" + nextStatus);
            updatedRows = approvalLogMapper.updatePolicyForSubmit(request.getPolicyId(), request.getUserId(), "SIGNING");
            System.out.println("實際更新行數: " + updatedRows);
        } 
        else if ("MANAGER".equals(request.getRole())) {
            // 主管審核：檢查是否為 SIGNING
            nextStatus = "APPROVE".equals(request.getAction()) ? "FINISH" : "REJECTED";
            updatedRows = approvalLogMapper.updatePolicyForManager(request.getPolicyId(), request.getUserId(), nextStatus);
        }
        if ("MANAGER".equals(request.getRole()) && "REJECT".equals(request.getAction())) {
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new IllegalArgumentException("駁回時必須填寫退回原因");
            }
        }

        //  防呆機制：若受影響列數為0，代表狀態已被搶先變更
        if (updatedRows == 0) {
            throw new IllegalStateException("此單據狀態已改變，無法執行此動作！");
        }
        //轉換為 Entity
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
        if ("SALESMAN".equals(role)) {
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
    //透過登入的角色權限role&代號userId查詢該權限負責的保單資料
    public List<Map<String, Object>> getPolicyList(Long userId, String role) {
        return approvalLogMapper.findPoliciesByRole(userId, role);
    }

    public List<ApprovalLog> getLogsByPolicyId(Long policyId, Long userId, String role) {
        // 檢查保單所屬權限
        Map<String, Object> policy = approvalLogMapper.findPolicyById(policyId);
        if (policy == null) {
            throw new IllegalArgumentException("保單不存在");
        }
        // 如果是業務員，必須確認保單是他自己的
        if ("SALESMAN".equals(role)) {
            Long ownerId = (Long) policy.get("agent_id"); 
            if (!ownerId.equals(userId)) {
                throw new IllegalArgumentException("無權存取此保單記錄");
            }
        }
        
        return approvalLogMapper.findLogsByPolicyId(policyId);
    }
    
    public List<Map<String, Object>> getWorklist(Long userId, String role) {
        return approvalLogMapper.findWorklistByRole(userId, role);
    }
    
    public Map<String, Object> getPolicyById(Long policyId) {
        return approvalLogMapper.findPolicyById(policyId);
    }
    
}
