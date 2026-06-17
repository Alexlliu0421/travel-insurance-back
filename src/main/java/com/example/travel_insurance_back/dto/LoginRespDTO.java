package com.example.travel_insurance_back.dto;

// 登入成功後回傳給前端的資料
// 包含 userId、role、token 三個欄位
public class LoginRespDTO {
    // 欄位
    private Long userId;
    private String role;
    private String token;

    // getter / setter

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}