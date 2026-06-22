package com.example.travel_insurance_back.service;

import com.example.travel_insurance_back.dto.request.ForgotPasswordReqDTO;
import com.example.travel_insurance_back.dto.request.LoginReqDTO;
import com.example.travel_insurance_back.dto.request.RegisterReqDTO;
import com.example.travel_insurance_back.dto.request.ResetPasswordReqDTO;
import com.example.travel_insurance_back.dto.response.LoginRespDTO;

// 處理登入、註冊、Email驗證、忘記密碼相關的業務邏輯
public interface AuthService {

    // 登入：驗證帳密，回傳 LoginRespDTO（含 token）
    LoginRespDTO login(LoginReqDTO loginReqDTO);

    // 註冊：建立帳號並寄送 Email 驗證信
    void register(RegisterReqDTO registerReqDTO);

    // Email 驗證：使用者點擊信件連結後呼叫，將帳號狀態改為已驗證
    void verifyEmail(String token);

    // 忘記密碼：使用者輸入 email，後端查無誤後寄送「重設密碼信」
    // 不會直接告知 email 是否存在於系統（避免被用來探測帳號），統一回應成功
    void forgotPassword(ForgotPasswordReqDTO forgotPasswordReqDTO);

    // 重設密碼：使用者點擊信件連結、輸入新密碼後呼叫，帶 token 驗證身份並更新密碼
    void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO);
}