package com.example.travel_insurance_back.controller;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class ApprovalController extends BaseController{

	@Autowired
	private ApprovalLogService approvalLogService;
	@Autowired
	private DataSource dataSource;

	@GetMapping("/db")
	public String testDb() {
		try (Connection conn = dataSource.getConnection()) {
			if (conn.isValid(2)) {
				return "資料庫連線成功！連線狀態正常。";
			} else {
				return "資料庫連線失敗：連接無效。";
			}
		} catch (Exception e) {
			return "連線發生異常: " + e.getMessage();
		}
	}

	@PostMapping("/submit")
	public ResponseEntity<String> submit(@RequestBody ApprovalRequest approvalRequest, HttpServletRequest httpServletRequest) {
		try {
			System.out.println("-------------------進入ApprovalController submit 方法----------------");
			System.out.println("userId = " + getUserId(httpServletRequest));
			System.out.println("role = " + getRole(httpServletRequest));
			approvalRequest.setUserId(getUserId(httpServletRequest));
			approvalRequest.setRole(getRole(httpServletRequest));
			// 呼叫 Service 執行核心邏輯
			approvalLogService.addLog(approvalRequest);

			return ResponseEntity.ok("簽核動作已完成");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// 當 Service 拋出參數錯誤或權限不符時，回傳 400
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			// 捕捉其他未預期的系統錯誤 (例如資料庫連線失敗)
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系統異常，請稍後再試");
		}
	}
	//查詢不同權限負責的保單LIST
	@GetMapping("/policies")
	public ResponseEntity<?> getPolicyList(HttpServletRequest httpServletRequest) {
		return ResponseEntity.ok(approvalLogService.getPolicyList(getUserId(httpServletRequest), getRole(httpServletRequest)));
	}
	//查詢單筆保單的歷程記錄
	@GetMapping("/history/{policyId}")
	public ResponseEntity<?> getLogs(@PathVariable Long policyId, HttpServletRequest httpServletRequest) {
		return ResponseEntity.ok(approvalLogService.getLogsByPolicyId(policyId, getUserId(httpServletRequest), getRole(httpServletRequest)));
	}
	//查詢工作區內待審的保單LIST
	@GetMapping("/worklist")
	public ResponseEntity<?> getWorklist(HttpServletRequest httpServletRequest) {
		return ResponseEntity.ok(approvalLogService.getWorklist(getUserId(httpServletRequest), getRole(httpServletRequest)));
	}
	//查詢工作區內點擊後單筆保單資料
	@GetMapping("/policies/{policyId}")
	public ResponseEntity<?> getPolicyDetail(@PathVariable Long policyId) {
	    return ResponseEntity.ok(approvalLogService.getPolicyById(policyId));
	}

}
