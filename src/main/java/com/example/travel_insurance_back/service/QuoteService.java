package com.example.travel_insurance_back.service;

import com.example.travel_insurance_back.dto.request.QuoteRequestDto;
import com.example.travel_insurance_back.dto.response.QuoteResponseDto;

public interface QuoteService {
    QuoteResponseDto calculate(QuoteRequestDto request);
}