package com.example.travel_insurance_back.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.travel_insurance_back.entity.User;

// MyBatis Mapper 介面
// 負責對 users table 做 CRUD
@Mapper
public interface UserMapper {

    // 根據 email 查詢使用者（註冊檢查重複用）
    User findByEmail(String email);

    // 根據身分證字號查詢使用者（登入用）
    User findByIdNumber(String idNumber);

    // 新增使用者（註冊用）
    void insert(User user);

    // 根據 verify_token 查詢使用者（Email 驗證用）
    User findByVerifyToken(String verifyToken);

    // 更新 is_verified 為 true，清除 verify_token（Email 驗證完成）
    void verifyEmail(Long id);

    // 前台更新使用者資料（個人資料修改用，Bolin 的 ProfileServiceImpl 會呼叫）
    void updateProfile(User user);

    User findById(Long id);
}