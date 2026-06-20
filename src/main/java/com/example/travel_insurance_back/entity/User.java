package com.example.travel_insurance_back.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String idNumber;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String role;
    private Boolean isVerified;
    private String verifyToken;
    private String status;
    private LocalDateTime createdDate;
    private LocalDate birthDate;
    private String nationality;
    private String gender;
    private LocalDateTime updatedDate;
    private String occupationName;
}