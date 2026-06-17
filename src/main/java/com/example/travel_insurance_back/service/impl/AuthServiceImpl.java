package com.example.travel_insurance_back.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.travel_insurance_back.dto.LoginReqDTO;
import com.example.travel_insurance_back.dto.LoginRespDTO;
import com.example.travel_insurance_back.dto.RegisterReqDTO;
import com.example.travel_insurance_back.entity.User;
import com.example.travel_insurance_back.mapper.UserMapper;
import com.example.travel_insurance_back.security.JwtTokenProvider;
import com.example.travel_insurance_back.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    // 1. 用 email 查詢 user
    // 2. 檢查 user 是否存在
    // 3. 檢查密碼是否正確（passwordEncoder.matches）
    // 4. 檢查 is_verified 是否為 true
    // 5. 產生 token
    // 6. 回傳 LoginRespDTO 物件
    public LoginRespDTO login(LoginReqDTO loginReqDTO) {
        User user = userMapper.findByEmail(loginReqDTO.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(loginReqDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        if (!user.getIsVerified()) {
            throw new RuntimeException("Email not verified");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        LoginRespDTO resp = new LoginRespDTO();
        resp.setUserId(user.getId());
        resp.setRole(user.getRole());
        resp.setToken(token);
        return resp;

    }

    @Override
    // 1. 檢查 email 是否已存在
    // 2. 密碼加密（passwordEncoder.encode）
    // 3. 產生 verify_token（UUID）
    // 4. 建立 User 物件存進 db
    // 5. 寄驗證信（先留空，之後加）
    public void register(RegisterReqDTO registerReqDTO) {
        User existingUser = userMapper.findByEmail(registerReqDTO.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("Email already exists");
        }
        String encodedPassword = passwordEncoder.encode(registerReqDTO.getPassword());
        String verifyToken = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail(registerReqDTO.getEmail());
        user.setPassword(encodedPassword);
        user.setIsVerified(false);
        user.setVerifyToken(verifyToken);
        // 在 userMapper.insert(user) 之前補上
        user.setName(registerReqDTO.getName());
        user.setIdNumber(registerReqDTO.getIdNumber());
        user.setPhone(registerReqDTO.getPhone());
        user.setAddress(registerReqDTO.getAddress());
        user.setRole("USER"); // 預設角色
        user.setStatus("ACTIVE");
        user.setBirthDate(registerReqDTO.getBirthDate());
        user.setNationality(registerReqDTO.getNationality());
        user.setGender(registerReqDTO.getGender());
        userMapper.insert(user);
    }

    @Override
    // 1. 用 token 查詢 user
    // 2. 檢查 user 是否存在
    // 3. 更新 is_verified = true，清除 verify_token
    public void verifyEmail(String token) {
        User user = userMapper.findByVerifyToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid token");
        }
        userMapper.verifyEmail(user.getId());
    }
}