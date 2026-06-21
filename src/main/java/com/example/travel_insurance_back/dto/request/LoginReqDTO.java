package com.example.travel_insurance_back.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 登入請求：改用身分證字號登入，不再用 Email
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginReqDTO {
    private String idNumber;
    private String password;
}