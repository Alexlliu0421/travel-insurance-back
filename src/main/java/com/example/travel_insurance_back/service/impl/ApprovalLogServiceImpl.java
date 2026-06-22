package com.example.travel_insurance_back.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_insurance_back.dto.ApprovalRequest;
import com.example.travel_insurance_back.entity.ApprovalLog;
import com.example.travel_insurance_back.mapper.ApprovalLogMapper;
import com.example.travel_insurance_back.service.ApprovalLogService;
import com.example.travel_insurance_back.service.EmailService;

@Service
public class ApprovalLogServiceImpl implements ApprovalLogService {

	@Autowired
	private ApprovalLogMapper approvalLogMapper;
	@Autowired
	private EmailService emailService;

	@Override
	@Transactional
	public void addLog(ApprovalRequest request) {
	    // 1. 預先檢查
	    validateRejectRemark(request);
	    String validAction = validateAndGetAction(request.getRole(), request.getAction());
	    
	    // 2. 執行業務邏輯
	    int updatedRows = executeStatusUpdate(request);
	    
	    // 3. 處理防呆
	    if (updatedRows == 0) {
	        throw new IllegalStateException("此單據狀態已改變，無法執行此動作！");
	    }
	    
	    // 4. 處理寄信邏輯 
	    String action = request.getAction();
	    if ("APPROVE".equals(action) || "REJECT".equals(action)) {
	        Long policyId = request.getPolicyId();
	        String remark = request.getRemark();
	        
	        CompletableFuture.runAsync(() -> {
	            // 在非同步內部才執行 Mapper 查詢，減少主執行緒壓力
	            String policyNumber = approvalLogMapper.findPolicyNumberById(policyId);
	            
	            if ("APPROVE".equals(action)) {
	                String userEmail = approvalLogMapper.findUserEmailByPolicyId(policyId);
	                if (userEmail != null) {
	                    emailService.sendPolicyActivatedEmail(userEmail, policyNumber);
	                }
	            } else if ("REJECT".equals(action)) {
	                String agentEmail = approvalLogMapper.findAgentEmailByPolicyId(policyId);
	                if (agentEmail != null) {
	                    emailService.sendPolicyRejectedEmail(agentEmail, policyNumber, remark);
	                }
	            }
	        });
	    }
	    
	    // 5. 記錄歷程
	    saveApprovalLog(request, validAction);
	}

	// 拆解：駁回原因檢查
	private void validateRejectRemark(ApprovalRequest request) {
		if ("MANAGER".equals(request.getRole()) && "REJECT".equals(request.getAction())) {
			if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
				throw new IllegalArgumentException("駁回時必須填寫退回原因");
			}
		}
	}

	// 拆解：狀態更新邏輯
	private int executeStatusUpdate(ApprovalRequest request) {
		if ("SALESMAN".equals(request.getRole()) && "SUBMIT".equals(request.getAction())) {
			return approvalLogMapper.updatePolicyForSubmit(request.getPolicyId(), request.getUserId(), "SIGNING");
		}

		if ("MANAGER".equals(request.getRole())) {
			String nextStatus = "APPROVE".equals(request.getAction()) ? "FINISH" : "REJECTED";
			return approvalLogMapper.updatePolicyForManager(request.getPolicyId(), request.getUserId(), nextStatus);
		}

		return 0; // 若角色不符或動作不對，預設不更新
	}

	// 拆解：寫入歷程
	private void saveApprovalLog(ApprovalRequest request, String action) {
		ApprovalLog log = new ApprovalLog();
		log.setPolicyId(request.getPolicyId());
		log.setOperatorId(request.getUserId());
		log.setAction(action);
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

	// 透過登入的角色權限role&代號userId查詢該權限負責的保單資料
	public List<Map<String, Object>> getPolicyList(Long userId, String role) {
		// 1. 安全性防禦：檢查 userId 是否為空
		if (userId == null) {
			throw new IllegalArgumentException("使用者身分驗證失敗，請重新登入");
		}

		// 2. 嚴格的角色檢查 (防範非法角色)
		if (!"SALESMAN".equals(role) && !"MANAGER".equals(role)) {
			throw new IllegalArgumentException("無效的角色存取權限");
		}

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

	public Map<String, Object> getPolicyById(Long policyId, Long userId, String role) {
		Map<String, Object> policy = approvalLogMapper.findPolicyById(policyId);

		if (policy == null) {
			throw new IllegalArgumentException("找不到此保單資料");
		}

		String status = (String) policy.get("status");
		Long agentId = (Long) policy.get("agent_id"); // 可能是 NULL

		if ("SALESMAN".equals(role)) {
			// 核心邏輯修正：
			// 1. 如果是 DRAFT 狀態，且沒有 ownerId (未被領取)，則允許所有業務員查看
			if ("DRAFT".equals(status) && agentId == null) {
				return policy; // 放行，所有人可看
			}

			// 2. 如果已經有 ownerId，則只有該 owner 可以看
			if (agentId != null && !agentId.equals(userId)) {
				throw new IllegalArgumentException("無權存取此保單明細 (已被其他業務員領取)");
			}

			// 3. 補充：如果單據狀態已經離開 DRAFT (例如在審核中)，也要檢查 owner 是否為自己
			if (!"DRAFT".equals(status) && (agentId == null || !agentId.equals(userId))) {
				throw new IllegalArgumentException("無權存取此保單明細");
			}
		}

		return policy;
	}

}
