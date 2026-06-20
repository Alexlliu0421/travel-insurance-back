package com.example.travel_insurance_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDto {
    private Long id;
    private String name;
    private String idNumber;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private LocalDate birthDate;
    private String nationality;
    private String occupationName;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}