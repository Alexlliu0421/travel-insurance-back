package com.example.travel_insurance_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_insurance_back.common.ApiResponse;
import com.example.travel_insurance_back.dto.LoginReqDTO;
import com.example.travel_insurance_back.dto.LoginRespDTO;
import com.example.travel_insurance_back.dto.RegisterReqDTO;
import com.example.travel_insurance_back.service.AuthService;

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
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterReqDTO registerReqDTO) {
        authService.register(registerReqDTO);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // GET /auth/verify
    // 接收 token（@RequestParam），回傳 ApiResponse<Void>
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // POST /auth/logout
    // 回傳 ApiResponse<Void>
    // 不需要做什麼，前端清除 token 就好

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}