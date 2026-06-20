package com.example.travel_insurance_back.service;

import com.example.travel_insurance_back.dto.request.ProfileUpdateRequestDto;
import com.example.travel_insurance_back.dto.response.ProfileResponseDto;

public interface ProfileService {
    ProfileResponseDto findMyProfile(Long userId);
    ProfileResponseDto updateMyProfile(Long userId, ProfileUpdateRequestDto request);
}