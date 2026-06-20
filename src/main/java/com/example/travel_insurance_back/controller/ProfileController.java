package com.example.travel_insurance_back.controller;

import com.example.travel_insurance_back.common.ApiResponse;
import com.example.travel_insurance_back.dto.request.ProfileUpdateRequestDto;
import com.example.travel_insurance_back.dto.response.ProfileResponseDto;
import com.example.travel_insurance_back.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "個人資料")
@RestController
@RequestMapping("/client/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "取得當前登入者資料")
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(profileService.findMyProfile(userId)));
    }

    @Operation(summary = "修改個人資料")
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponseDto>> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequestDto request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateMyProfile(userId, request)));
    }
}