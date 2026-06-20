package com.example.travel_insurance_back.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestDto {
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalDate insuredBirthDate;
    private Integer insuredGender;       // 0:女 1:男
    private String insuredOccupationCode;
    private Long coverageId;
}