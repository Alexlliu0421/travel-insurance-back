package com.example.travel_insurance_back.service;

// 負責寄送 Email 驗證信
public interface EmailService {

    // 寄送驗證信給指定信箱
    // 內容包含一個驗證連結，前端使用者點擊後呼叫 /api/auth/verify
    void sendVerificationEmail(String toEmail, String token);
    void sendPolicyActivatedEmail(String toEmail, String policyNumber);
	void sendPolicyRejectedEmail(String toEmail, String policyNumber, String remark);
}