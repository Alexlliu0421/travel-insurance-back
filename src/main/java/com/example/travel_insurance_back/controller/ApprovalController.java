package com.example.travel_insurance_back.controller;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_insurance_back.dto.ApprovalRequest;
import com.example.travel_insurance_back.service.ApprovalLogService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

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
	public ResponseEntity<String> submit(@RequestBody ApprovalRequest request, HttpServletRequest httpServletRequest) {
		try {
			// 從 Token 獲取當前使用者身分 (等待合併)
//          Integer userId = AuthUtils.getUserId(httpServletRequest);
//          String role = AuthUtils.getRole(httpServletRequest);

			// swagger測試
			Integer userId = Integer.valueOf(httpServletRequest.getHeader("X-User-Id"));
			String role = httpServletRequest.getHeader("X-Role");
			request.setUserId(userId);
			request.setRole(role);

			// 呼叫 Service 執行核心邏輯
			approvalLogService.addLog(request);

			return ResponseEntity.ok("簽核動作已完成");
		} catch (IllegalArgumentException e) {
			// 當 Service 拋出參數錯誤或權限不符時，回傳 400
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

		} catch (Exception e) {
			// 捕捉其他未預期的系統錯誤 (例如資料庫連線失敗)
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系統異常，請稍後再試");
		}
	}
}
