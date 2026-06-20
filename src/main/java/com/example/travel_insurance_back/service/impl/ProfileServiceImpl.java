package com.example.travel_insurance_back.service.impl;

import com.example.travel_insurance_back.dto.request.ProfileUpdateRequestDto;
import com.example.travel_insurance_back.dto.response.ProfileResponseDto;
import com.example.travel_insurance_back.entity.User;
import com.example.travel_insurance_back.mapper.UserMapper;
import com.example.travel_insurance_back.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;

    @Override
    public ProfileResponseDto findMyProfile(Long userId) {
        User user = userMapper.findById(userId);
        validateUserExists(user);
        return toProfileResponse(user);
    }

    @Override
    public ProfileResponseDto updateMyProfile(Long userId, ProfileUpdateRequestDto request) {
        User user = userMapper.findById(userId);
        validateUserExists(user);

        User updated = new User();
        updated.setId(userId);
        updated.setName(request.getName());
        updated.setPhone(request.getPhone());
        updated.setAddress(request.getAddress());
        updated.setNationality(request.getNationality());
        updated.setOccupationName(request.getOccupationName());
        updated.setUpdatedDate(LocalDateTime.now());

        userMapper.updateProfile(updated);

        return findMyProfile(userId);
    }

    // --- 驗證 ---

    private void validateUserExists(User user) {
        if (user == null)
            throw new IllegalArgumentException("查無使用者");
    }

    // --- 轉換 ---

    private ProfileResponseDto toProfileResponse(User user) {
        return ProfileResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .idNumber(user.getIdNumber())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .nationality(user.getNationality())
                .occupationName(user.getOccupationName())
                .status(user.getStatus())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .build();
    }
}