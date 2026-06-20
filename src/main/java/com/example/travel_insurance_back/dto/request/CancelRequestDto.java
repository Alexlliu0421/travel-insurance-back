package com.example.travel_insurance_back.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequestDto {
    private Long policyId;
    private String reason;  // 取消原因（選填）
}