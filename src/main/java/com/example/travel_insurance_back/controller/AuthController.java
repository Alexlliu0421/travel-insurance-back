package com.example.travel_insurance_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_insurance_back.common.ApiResponse;
import com.example.travel_insurance_back.dto.request.ForgotPasswordReqDTO;
import com.example.travel_insurance_back.dto.request.LoginReqDTO;
import com.example.travel_insurance_back.dto.request.RegisterReqDTO;
import com.example.travel_insurance_back.dto.request.ResetPasswordReqDTO;
import com.example.travel_insurance_back.dto.response.LoginRespDTO;
import com.example.travel_insurance_back.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "帳號驗證", description = "註冊、登入、Email驗證、登出相關 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /auth/login
    // 接收 LoginReqDTO，回傳 ApiResponse<LoginRespDTO>
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginRespDTO>> login(@RequestBody LoginReqDTO loginReqDTO) {
        LoginRespDTO loginRespDTO = authService.login(loginReqDTO);
        return ResponseEntity.ok(ApiResponse.success(loginRespDTO));
    }

    // POST /auth/register
    // 接收 RegisterReqDTO，回傳 ApiResponse<Void>
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterReqDTO registerReqDTO) {
        authService.register(registerReqDTO);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // GET /auth/verify
    // 接收 token，驗證成功或失敗都顯示簡單的 HTML 頁面（不是 JSON）
    @GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            String html = "<html><body style='text-align:center; font-family:sans-serif; margin-top:100px;'>"
                    + "<h2>✅ 驗證成功！</h2>"
                    + "<p>您的帳號已成功驗證，請返回登入頁登入。</p>"
                    + "</body></html>";
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            String html = "<html><body style='text-align:center; font-family:sans-serif; margin-top:100px;'>"
                    + "<h2>❌ 驗證失敗</h2>"
                    + "<p>" + e.getMessage() + "</p>"
                    + "</body></html>";
            return ResponseEntity.ok(html);
        }
    }

    // POST /auth/logout
    // 回傳 ApiResponse<Void>
    // 不需要做什麼，前端清除 token 就好

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // POST /auth/forgot-password
    // 接收 ForgotPasswordReqDTO，回傳 ApiResponse<Void>
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordReqDTO forgotPasswordReqDTO) {
        authService.forgotPassword(forgotPasswordReqDTO);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // POST /auth/reset-password
    // 接收 ResetPasswordReqDTO，回傳 ApiResponse<Void>
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordReqDTO resetPasswordReqDTO) {
        authService.resetPassword(resetPasswordReqDTO);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // GET /auth/check-email?email=xxx
    // 回傳 ApiResponse<Boolean>，true 代表已被註冊
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // GET /auth/check-idnumber?idNumber=xxx
    // 回傳 ApiResponse<Boolean>，true 代表已被註冊
    @GetMapping("/check-idnumber")
    public ResponseEntity<ApiResponse<Boolean>> checkIdNumber(@RequestParam String idNumber) {
        boolean exists = authService.checkIdNumberExists(idNumber);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}