package com.example.travel_insurance_back.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequestDto {
    private String name;
    private String phone;
    private String address;
    private String nationality;
    private String occupationName;
}