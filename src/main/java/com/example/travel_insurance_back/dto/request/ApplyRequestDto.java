package com.example.travel_insurance_back.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyRequestDto {
    // 被保人資料
    private String insuredName;
    private String insuredIdNumber;
    private LocalDate insuredBirthDate;
    private Integer insuredGender;       // 0:女 1:男
    private String insuredOccupationCode;

    // 旅遊資訊
    private LocalDate departureDate;
    private LocalDate returnDate;

    // 保額選擇
    private Long coverageId;
}