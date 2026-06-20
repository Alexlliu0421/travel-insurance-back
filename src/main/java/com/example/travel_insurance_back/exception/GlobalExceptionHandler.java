package com.example.travel_insurance_back.exception;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.travel_insurance_back.common.ApiResponse;
import com.example.travel_insurance_back.common.ResultCode;

// 全域例外處理器
// 統一攔截所有例外，包裝成 ApiResponse 格式回傳給前端
// 避免每個 Controller 各自處理例外，集中管理
// 前端目前未讀取 msg 欄位，主要用途是確保 HTTP 狀態碼正確，以及 e.printStackTrace() 協助後端除錯
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 找不到資料時拋出 → 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NoSuchElementException e) {
        return ResponseEntity.status(404).body(ApiResponse.error(ResultCode.NOT_FOUND));
    }

    // 參數錯誤時拋出（如查詢區間為空、流水號為空）→ 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(ApiResponse.error(ResultCode.BAD_REQUEST));
    }

    // 登入失敗時拋出 → 401
    // 不印 e.printStackTrace()，帳密錯誤是預期內的情況，不是系統錯誤
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(401).body(ApiResponse.error(ResultCode.UNAUTHORIZED));
    }

    // 呼叫不存在的 API 路徑 → 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ResponseEntity.status(404).body(ApiResponse.error(ResultCode.NOT_FOUND));
    }

    // 兜底，所有非預期例外 → 500
    // e.printStackTrace() → 印出完整堆疊，方便開發者除錯
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(ApiResponse.error(ResultCode.SERVER_ERROR));
    }

    // Email 格式等驗證失敗時拋出 → 400
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(message);
        return ResponseEntity.status(400).body(response);
    }
}