package com.example.travel_insurance_back.controller;

import com.example.travel_insurance_back.common.ApiResponse;
import com.example.travel_insurance_back.dto.request.CancelRequestDto;
import com.example.travel_insurance_back.dto.response.PlansResponseDto;
import com.example.travel_insurance_back.dto.response.PolicyResponseDto;
import com.example.travel_insurance_back.service.PlansService;
import com.example.travel_insurance_back.util.PdfGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "保單管理")
@RestController
@RequestMapping("/client/plans")
@RequiredArgsConstructor
public class PlansController {

    private final PlansService plansService;
    private final PdfGenerator pdfGenerator;

    @Operation(summary = "查詢我的保單列表")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlansResponseDto>>> findMyPlans(
            @AuthenticationPrincipal Long applicantId) {
        return ResponseEntity.ok(ApiResponse.success(plansService.findMyPlans(applicantId)));
    }

    @Operation(summary = "申請取消保單")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @Valid @RequestBody CancelRequestDto request,
            @AuthenticationPrincipal Long applicantId) {
        plansService.cancel(request, applicantId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "下載保單 PDF")
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestParam Long policyId,
            @AuthenticationPrincipal Long applicantId) {
        PolicyResponseDto policy = plansService.findMyPlanDetail(policyId, applicantId);
        byte[] pdfBytes = pdfGenerator.generatePolicyPdf(policy);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", policy.getPolicyNumber() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}