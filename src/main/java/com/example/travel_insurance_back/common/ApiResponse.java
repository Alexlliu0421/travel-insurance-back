package com.example.travel_insurance_back.common;
// 1. 三個欄位：code、message、data

// 2. data 用泛型 <T>

// 3. 靜態方法 success(data) 
// 4. 靜態方法 error(code, message)
// 5. 用 ResultCode 來取得 code 和 message
public class ApiResponse<T> {

    private int code;
    private String message;
    // 2. data 用泛型 <T>
    private T data;
    // 不管回傳什麼，格式都一樣：
    // 回傳格式：{ "code": 200, "message": "成功", "data": 真正的資料 }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ResultCode.SUCCESS.getCode()); // 設定狀態碼 200
        apiResponse.setMessage(ResultCode.SUCCESS.getMessage()); // 設定訊息 "成功"
        apiResponse.setData(data); // 放入實際回傳的資料 // // 放入 JWT token 字串
        return apiResponse; // 回傳包裝好的 ApiResponse
    }// success 一定是 200，所以直接寫死 ResultCode.SUCCESS。

    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        ApiResponse<T> apiResponse = new ApiResponse<>();
        apiResponse.setCode(resultCode.getCode()); // 由呼叫方決定錯誤碼 (404/400/500)
        apiResponse.setMessage(resultCode.getMessage()); // 對應的錯誤訊息
        return apiResponse; // 回傳包裝好的錯誤 ApiResponse，data 為 null
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
