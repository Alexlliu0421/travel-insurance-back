package com.example.travel_insurance_back.common;

// 統一管理 HTTP 狀態碼與訊息
// 用 enum 限定只有這幾種狀態，比用 int 或 String 更安全，不會出現不存在的狀態碼
public enum ResultCode {

    SUCCESS(200, "成功"),
    NOT_FOUND(404, "找不到"),
    BAD_REQUEST(400, "參數錯誤"),
    SERVER_ERROR(500, "伺服器錯誤"),
    UNAUTHORIZED(401, "未授權");

    private final int code; // HTTP 狀態碼
    private final String message; // 對應訊息

    // enum 建構子，每個常數建立時自動呼叫
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // final → 欄位一旦賦值不可修改，確保狀態碼不會被任何地方改動
    // 只提供 Getter，不提供 Setter
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
