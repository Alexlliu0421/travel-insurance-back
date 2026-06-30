package com.example.travel_insurance_back.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.example.travel_insurance_back.dto.ApprovalRequest;
import com.example.travel_insurance_back.entity.ApprovalLog;
import com.example.travel_insurance_back.exception.BusinessException;
import com.example.travel_insurance_back.mapper.ApprovalLogMapper;
import com.example.travel_insurance_back.service.ApprovalLogService;
import com.example.travel_insurance_back.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalLogServiceImpl implements ApprovalLogService {

    // 建議將這些常數移至專屬的 Enum 類別中，此處先以常數定義
    private static final String ROLE_SALESMAN = "SALESMAN";
    private static final String ROLE_MANAGER = "MANAGER";
    
    private static final String ACTION_SUBMIT = "SUBMIT";
    private static final String ACTION_APPROVE = "APPROVE";
    private static final String ACTION_REJECT = "REJECT";
    
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SIGNING = "SIGNING";
    private static final String STATUS_FINISH = "FINISH";
    private static final String STATUS_REJECTED = "REJECTED";

    // 採用建構子注入 (由 Lombok @RequiredArgsConstructor 自動產生)
    private final ApprovalLogMapper approvalLogMapper;
    private final EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLog(ApprovalRequest request) {
        validateRejectRemark(request);
        String validAction = validateAndGetAction(request.getRole(), request.getAction());
        
        int updatedRows = executeStatusUpdate(request);
        if (updatedRows == 0) {
            throw new BusinessException("此單據狀態已改變，無法執行此動作！");
        }
        
        saveApprovalLog(request, validAction);
        handlePostCommitEmail(request);
    }

    private void handlePostCommitEmail(ApprovalRequest request) {
        String action = request.getAction();
        if (!ACTION_APPROVE.equals(action) && !ACTION_REJECT.equals(action)) {
            return;
        }

        Long policyId = request.getPolicyId();
        String remark = request.getRemark();
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> sendEmailSafely(action, policyId, remark));
            }
        });
    }

    private void sendEmailSafely(String action, Long policyId, String remark) {
        try {
            String policyNumber = approvalLogMapper.findPolicyNumberById(policyId);
            
            if (ACTION_APPROVE.equals(action)) {
                String userEmail = approvalLogMapper.findUserEmailByPolicyId(policyId);
                if (StringUtils.hasText(userEmail)) {
                    emailService.sendPolicyActivatedEmail(userEmail, policyNumber);
                }
            } else if (ACTION_REJECT.equals(action)) {
                String agentEmail = approvalLogMapper.findAgentEmailByPolicyId(policyId);
                if (StringUtils.hasText(agentEmail)) {
                    emailService.sendPolicyRejectedEmail(agentEmail, policyNumber, remark);
                }
            }
        } catch (Exception e) {
            log.error("非同步郵件發送失敗, PolicyId: {}, Error: {}", policyId, e.getMessage(), e);
        }
    }

    private void validateRejectRemark(ApprovalRequest request) {
        if (ROLE_MANAGER.equals(request.getRole()) && ACTION_REJECT.equals(request.getAction())) {
            if (!StringUtils.hasText(request.getRemark())) {
                throw new BusinessException("駁回時必須填寫退回原因");
            }
        }
    }

    private int executeStatusUpdate(ApprovalRequest request) {
        if (ROLE_SALESMAN.equals(request.getRole()) && ACTION_SUBMIT.equals(request.getAction())) {
            return approvalLogMapper.updatePolicyForSubmit(request.getPolicyId(), request.getUserId(), STATUS_SIGNING);
        }
        if (ROLE_MANAGER.equals(request.getRole())) {
            String nextStatus = ACTION_APPROVE.equals(request.getAction()) ? STATUS_FINISH : STATUS_REJECTED;
            return approvalLogMapper.updatePolicyForManager(request.getPolicyId(), request.getUserId(), nextStatus);
        }
        return 0; 
    }

    private void saveApprovalLog(ApprovalRequest request, String action) {
        ApprovalLog logEntity = new ApprovalLog();
        logEntity.setPolicyId(request.getPolicyId());
        logEntity.setOperatorId(request.getUserId());
        logEntity.setAction(action);
        logEntity.setRemark(request.getRemark());
        logEntity.setCreatedDate(LocalDateTime.now());
        approvalLogMapper.insert(logEntity);
    }

    private String validateAndGetAction(String role, String action) {
        if (ROLE_SALESMAN.equals(role)) {
            if (!ACTION_SUBMIT.equals(action)) {
                throw new BusinessException("業務員只能執行送審動作");
            }
        } else if (ROLE_MANAGER.equals(role)) {
            if (!ACTION_APPROVE.equals(action) && !ACTION_REJECT.equals(action)) {
                throw new BusinessException("主管只能執行核准或駁回動作");
            }
        } else {
            throw new BusinessException("未知角色，拒絕存取");
        }
        return action;
    }

    public List<Map<String, Object>> getPolicyList(Long userId, String role) {
        if (userId == null) {
            throw new BusinessException("使用者身分驗證失敗，請重新登入");
        }
        if (!ROLE_SALESMAN.equals(role) && !ROLE_MANAGER.equals(role)) {
            throw new BusinessException("無效的角色存取權限");
        }
        return approvalLogMapper.findPoliciesByRole(userId, role);
    }

    public List<ApprovalLog> getLogsByPolicyId(Long policyId, Long userId, String role) {
        Map<String, Object> policy = approvalLogMapper.findPolicyById(policyId);
        if (policy == null) {
            throw new BusinessException("保單不存在");
        }
        
        if (ROLE_SALESMAN.equals(role)) {
            Long ownerId = getSafeLong(policy.get("agent_id"));
            if (!userId.equals(ownerId)) {
                throw new BusinessException("無權存取此保單記錄");
            }
        }
        return approvalLogMapper.findLogsByPolicyId(policyId);
    }

    public List<Map<String, Object>> getWorklist(Long userId, String role) {
        if (!ROLE_SALESMAN.equals(role) && !ROLE_MANAGER.equals(role)) {
            throw new BusinessException("無效的角色，無權限存取工作清單");
        }
        List<Map<String, Object>> result = approvalLogMapper.findWorklistByRole(userId, role);
        log.debug(">> role: {}", role);
        log.debug(">>> 服務層撈出的資料: {}", result);
        return result; // 修正：直接回傳已經查詢到的變數，避免二次呼叫 DB
    }

    public Map<String, Object> getPolicyById(Long policyId, Long userId, String role) {
        Map<String, Object> policy = approvalLogMapper.findPolicyById(policyId);
        if (policy == null) {
            throw new BusinessException("找不到此保單資料");
        }

        String status = (String) policy.get("status");
        Long agentId = getSafeLong(policy.get("agent_id"));

        if (ROLE_SALESMAN.equals(role)) {
            if (STATUS_DRAFT.equals(status) && agentId == null) {
                return policy; 
            }
            if (agentId != null && !agentId.equals(userId)) {
                throw new BusinessException("無權存取此保單明細 (已被其他業務員領取)");
            }
            if (!STATUS_DRAFT.equals(status) && (agentId == null || !agentId.equals(userId))) {
                throw new BusinessException("無權存取此保單明細");
            }
        }
        return policy;
    }

    // 安全轉換 Number 為 Long，避免 ClassCastException
    private Long getSafeLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}