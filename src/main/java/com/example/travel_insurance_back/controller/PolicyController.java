package com.example.travel_insurance_back.controller;

import com.example.travel_insurance_back.common.ApiResponse;
import com.example.travel_insurance_back.dto.request.ApplyRequestDto;
import com.example.travel_insurance_back.dto.request.QuoteRequestDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto;
import com.example.travel_insurance_back.dto.response.QuoteResponseDto;
import com.example.travel_insurance_back.service.PolicyService;
import com.example.travel_insurance_back.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "保單試算與投保")
@RestController
@RequestMapping("/client/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final QuoteService quoteService;
    private final PolicyService policyService;

    @Operation(summary = "保單試算")
    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<QuoteResponseDto>> quote(
            @Valid @RequestBody QuoteRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(quoteService.calculate(request)));
    }

    @Operation(summary = "送出投保")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<PolicyResponseDto>> apply(
            @Valid @RequestBody ApplyRequestDto request,
            @AuthenticationPrincipal Long applicantId) {
        return ResponseEntity.ok(ApiResponse.success(policyService.apply(applicantId, request)));
    }
}