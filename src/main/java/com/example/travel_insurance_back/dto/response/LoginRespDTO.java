package com.example.travel_insurance_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 登入成功後回傳給前端的資料
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespDTO {
    private Long userId;
    private String name;
    private String role;
    private String token;
}