package com.example.travel_insurance_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_insurance_back.dto.ApprovalRequest;
import com.example.travel_insurance_back.service.ApprovalLogService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/approval")
@CrossOrigin(origins = "http://localhost:5173")
public class ApprovalController extends BaseController {

    @Autowired
    private ApprovalLogService approvalLogService;

    @PostMapping("/submit")
    public ResponseEntity<String> submit(@RequestBody ApprovalRequest approvalRequest, HttpServletRequest httpServletRequest) {
        Long userId = getUserId(httpServletRequest);
        validateLogin(userId);
        
        approvalRequest.setUserId(userId);
        approvalRequest.setRole(getRole(httpServletRequest));
        
        // 核心邏輯：異常由 GlobalExceptionHandler 處理，無需 try-catch
        approvalLogService.addLog(approvalRequest);

        return ResponseEntity.ok("簽核動作已完成");
    }

    @GetMapping("/policies")
    public ResponseEntity<?> getPolicyList(HttpServletRequest httpServletRequest) {
        Long userId = getUserId(httpServletRequest);
        validateLogin(userId);
        
        // 直接回傳結果，無需處理異常
        return ResponseEntity.ok(approvalLogService.getPolicyList(userId, getRole(httpServletRequest)));
    }

    @GetMapping("/history/{policyId}")
    public ResponseEntity<?> getLogs(@PathVariable Long policyId, HttpServletRequest httpServletRequest) {
        Long userId = getUserId(httpServletRequest);
        validateLogin(userId);
        
        return ResponseEntity.ok(approvalLogService.getLogsByPolicyId(policyId, userId, getRole(httpServletRequest)));
    }

    @GetMapping("/worklist")
    public ResponseEntity<?> getWorklist(HttpServletRequest httpServletRequest) {
        Long userId = getUserId(httpServletRequest);
        validateLogin(userId);
        
        return ResponseEntity.ok(approvalLogService.getWorklist(userId, getRole(httpServletRequest)));
    }

    @GetMapping("/policies/{policyId}")
    public ResponseEntity<?> getPolicyDetail(@PathVariable Long policyId, HttpServletRequest httpServletRequest) {
        Long userId = getUserId(httpServletRequest);
        validateLogin(userId);
        
        // 直接回傳結果，若 service 拋出異常，全域處理器會自動捕捉
        return ResponseEntity.ok(approvalLogService.getPolicyById(policyId, userId, getRole(httpServletRequest)));
    }
}