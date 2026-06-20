package com.example.travel_insurance_back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.travel_insurance_back.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    // 1. 組合驗證連結，格式: http://localhost:8080/api/auth/verify?token={token}
    // 2. 建立 SimpleMailMessage 物件
    // 3. 設定收件人 (toEmail)、主旨、內文（包含驗證連結）
    // 4. mailSender.send(message) 寄出
    public void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Travel Insurance - Verify Email");
        message.setText("http://localhost:8080/api/auth/verify?token=" + token);
        mailSender.send(message);

    }
}