package com.example.travel_insurance_back.service;

import com.example.travel_insurance_back.dto.request.LoginReqDTO;
import com.example.travel_insurance_back.dto.request.RegisterReqDTO;
import com.example.travel_insurance_back.dto.response.LoginRespDTO;

// 處理登入、註冊、Email驗證的業務邏輯
public interface AuthService {

    // 登入，回傳 LoginRespDTO
    LoginRespDTO login(LoginReqDTO loginReqDTO);

    // 註冊並寄驗證信
    void register(RegisterReqDTO registerReqDTO);

    // Email 驗證，啟用帳號
    void verifyEmail(String token);
}