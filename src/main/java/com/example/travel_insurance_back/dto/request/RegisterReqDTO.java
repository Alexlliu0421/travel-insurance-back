package com.example.travel_insurance_back.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReqDTO {

    private String name;
    private String idNumber;

    @Email(message = "Email 格式不正確")
    private String email;

    private String phone;
    private String address;
    private String password;
    private LocalDate birthDate;
    private String nationality;
    private String gender;
    private String occupationName;
}