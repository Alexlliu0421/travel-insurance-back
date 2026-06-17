package com.example.travel_insurance_back.exception;

// 自訂未授權例外，對應 HTTP 401
// Java 沒有內建 401 的例外，所以自訂此類別讓 GlobalExceptionHandler 精準攔截
// 繼承 RuntimeException → 不需要強制 try-catch，Spring 自動攔截
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        // super() → 呼叫父類別 RuntimeException 建構子
        // 訊息一路往上傳到 Throwable.detailMessage 存起來
        // GlobalExceptionHandler 攔截後，統一包裝成 ApiResponse 回傳 401 給前端
        super(message);
    }
}