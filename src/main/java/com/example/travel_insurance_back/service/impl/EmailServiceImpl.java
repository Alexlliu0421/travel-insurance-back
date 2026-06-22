package com.example.travel_insurance_back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.travel_insurance_back.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String link = "http://localhost:8080/api/auth/verify?token=" + token;

            String html = "<div style='font-family:Arial,sans-serif; background-color:#f4f6f8; padding:40px 0;'>"
                    + "  <div style='max-width:480px; margin:0 auto; background:#ffffff; border-radius:8px; "
                    + "overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.08);'>"
                    + "    <div style='background-color:#1976d2; padding:24px; text-align:center;'>"
                    + "      <h1 style='color:#ffffff; margin:0; font-size:20px;'>旅遊險平台</h1>"
                    + "    </div>"
                    + "    <div style='padding:32px 24px; text-align:center;'>"
                    + "      <h2 style='color:#333333; margin-top:0;'>驗證您的 Email</h2>"
                    + "      <p style='color:#555555; font-size:14px; line-height:1.6;'>"
                    + "感謝您註冊旅遊險平台！請點擊下方按鈕完成帳號驗證，即可開始使用我們的服務。"
                    + "      </p>"
                    + "      <a href='" + link + "' "
                    + "style='display:inline-block; margin:24px 0; padding:12px 32px; "
                    + "background-color:#1976d2; color:#ffffff; text-decoration:none; "
                    + "border-radius:6px; font-size:15px; font-weight:bold;'>"
                    + "立即驗證帳號"
                    + "      </a>"
                    + "      <p style='color:#999999; font-size:12px; margin-top:24px;'>"
                    + "此連結將於 24 小時後失效，請盡快完成驗證。"
                    + "      </p>"
                    + "      <p style='color:#999999; font-size:12px;'>"
                    + "若按鈕無法點擊，請複製以下網址至瀏覽器開啟：<br>"
                    + "<span style='color:#1976d2; word-break:break-all;'>" + link + "</span>"
                    + "      </p>"
                    + "    </div>"
                    + "    <div style='background-color:#f4f6f8; padding:16px; text-align:center;'>"
                    + "      <p style='color:#aaaaaa; font-size:11px; margin:0;'>"
                    + "此信件為系統自動發送，請勿直接回覆。"
                    + "      </p>"
                    + "    </div>"
                    + "  </div>"
                    + "</div>";

            helper.setTo(toEmail);
            helper.setSubject("旅遊險平台 - 請驗證您的 Email");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("寄送驗證信失敗", e);
        }
    }

    @Override
    public void sendResetPasswordEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 注意：這裡連結要導向前端頁面，不是後端 API
            // 因為使用者點擊後要填寫「設定新密碼」表單，不是後端直接處理完就結束
            String link = "http://localhost:5173/reset-password?token=" + token;

            String html = "<div style='font-family:Arial,sans-serif; background-color:#f4f6f8; padding:40px 0;'>"
                    + "  <div style='max-width:480px; margin:0 auto; background:#ffffff; border-radius:8px; "
                    + "overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.08);'>"
                    + "    <div style='background-color:#1976d2; padding:24px; text-align:center;'>"
                    + "      <h1 style='color:#ffffff; margin:0; font-size:20px;'>旅遊險平台</h1>"
                    + "    </div>"
                    + "    <div style='padding:32px 24px; text-align:center;'>"
                    + "      <h2 style='color:#333333; margin-top:0;'>重設您的密碼</h2>"
                    + "      <p style='color:#555555; font-size:14px; line-height:1.6;'>"
                    + "我們收到您的密碼重設請求。請點擊下方按鈕設定新密碼。"
                    + "      </p>"
                    + "      <a href='" + link + "' "
                    + "style='display:inline-block; margin:24px 0; padding:12px 32px; "
                    + "background-color:#1976d2; color:#ffffff; text-decoration:none; "
                    + "border-radius:6px; font-size:15px; font-weight:bold;'>"
                    + "重設密碼"
                    + "      </a>"
                    + "      <p style='color:#999999; font-size:12px; margin-top:24px;'>"
                    + "此連結將於 24 小時後失效。若您並未提出此請求，請忽略此信件。"
                    + "      </p>"
                    + "    </div>"
                    + "  </div>"
                    + "</div>";

            helper.setTo(toEmail);
            helper.setSubject("旅遊險平台 - 重設密碼請求");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("寄送重設密碼信失敗", e);
        }
    }
}