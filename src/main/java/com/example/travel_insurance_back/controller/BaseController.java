package com.example.travel_insurance_back.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.travel_insurance_back.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {
    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenProvider.getUserId(token);
    }
    
    protected String getRole(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenProvider.getRole(token);
    }
}
