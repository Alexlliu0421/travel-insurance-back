# 旅遊平安險投保系統 - 後端

## 專案簡介

本系統模擬旅遊平安險投保網站的完整流程，涵蓋會員系統、保費試算、線上投保、業務簽核四大環節。
採用 Spring Boot 前後端分離架構，後端負責業務邏輯與資料安全。

## 技術棧

- **框架**：Spring Boot 3.x
- **安全**：Spring Security + JWT（jjwt）
- **ORM**：MyBatis-Plus
- **資料庫**：MySQL
- **郵件**：Spring Boot Mail（JavaMailSender）
- **工具**：Lombok、Swagger（springdoc-openapi）、Maven

## 模組分工

| 模組 | 負責人 | 功能範圍 |
|---|---|---|
| 帳號模組 | - | 登入、註冊、Email 驗證、忘記密碼、權限管理 |
| 保單模組 | - | 保費試算、線上投保、個人資料、保單查詢、PDF 下載 |
| 簽核模組 | - | 簽核流程、狀態控管、歷程紀錄、自動通知 |

## 專案結構

```
src/main/java/com/example/travel_insurance_back/
├── config/
│   └── SecurityConfig.java          # Spring Security、CORS、角色權限
├── controller/
│   ├── AuthController.java          # 登入、註冊、Email 驗證、忘記密碼
│   ├── ProfileController.java       # 個人資料查詢與修改
│   ├── PlansController.java         # 保單查詢、取消、PDF 下載
│   ├── PolicyController.java        # 保費試算、線上投保
│   └── ApprovalController.java      # 簽核相關 API
├── dto/
│   ├── request/                     # ReqDTO：接收前端輸入
│   └── response/                    # RespDTO：回傳前端需要的欄位
├── entity/                          # 對應資料庫表格的 Java 物件
├── exception/
│   ├── GlobalExceptionHandler.java  # 統一例外處理（@RestControllerAdvice + @Slf4j）
│   ├── UnauthorizedException.java   # 自訂 401 例外
│   └── BusinessException.java       # 自訂業務邏輯例外
├── mapper/                          # MyBatis-Plus Mapper 介面 + XML
├── security/
│   ├── JwtTokenProvider.java        # JWT 產生、解析、驗證
│   └── JwtAuthenticationFilter.java # 每次請求驗證 token 身份
└── service/
    └── impl/
        ├── AuthServiceImpl.java         # 帳號模組業務邏輯
        ├── EmailServiceImpl.java        # 寄送驗證信、重設密碼信、簽核通知信
        ├── QuoteServiceImpl.java        # 保費試算邏輯
        ├── PolicyServiceImpl.java       # 投保流程
        ├── PlansServiceImpl.java        # 保單查詢、取消、PDF 下載
        ├── ProfileServiceImpl.java      # 個人資料管理
        └── ApprovalLogServiceImpl.java  # 簽核流程、歷程紀錄、非同步寄信
```

## 帳號模組

### 登入
- 使用身分證字號（非 Email）登入，符合保險業核對保戶身份的標準
- 密碼用 BCrypt 單向加密，`matches()` 比對，無法逆向還原
- 登入成功回傳 JWT token，前端存進 localStorage

### 註冊
- 身分證字號真檢查碼演算法驗證
- 身分證字號、Email 即時查重（後端查資料庫確保唯一性）
- 密碼 BCrypt 加密後存入資料庫
- 不管 Email 存不存在，回應相同訊息（帳號枚舉防護）

### Email 驗證流程
1. 使用者註冊後，系統寄送一封 HTML 格式的驗證信
2. 驗證連結使用 JWT 產生，帶有 `purpose = "EMAIL_VERIFY"` 標籤與 24 小時過期時間
3. 後端解析 token，確認用途標籤跟過期時間都正確才啟用帳號
4. 驗證成功後，使用者才能使用 `/api/auth/login` 登入

### 忘記密碼流程
1. 使用者輸入註冊 Email，系統寄送重設密碼連結
2. 連結使用 JWT 產生，帶有 `purpose = "RESET_PASSWORD"` 標籤與 24 小時過期時間
3. 兩種 token 共用同一套產生邏輯，purpose 標籤不同，無法互相冒用

### JWT 機制
- `JwtTokenProvider`：產生登入 token（帶 userId、role）、驗證 token、解析 purpose 標籤
- `JwtAuthenticationFilter`：繼承 `OncePerRequestFilter`，每次請求解析 token，把角色包成 `ROLE_xxx` 塞進 SecurityContext
- `SecurityFilterChain`：路徑規則由上而下匹配
  - `/api/auth/**` → permitAll（不需登入）
  - `/client/policy/quote` → permitAll（保費試算，訪客可用）
  - `/api/approval/**` → hasAnyRole("SALESMAN", "MANAGER")（角色限制）
  - `.anyRequest()` → authenticated（其他需要登入）

### 例外處理與 Log
- `GlobalExceptionHandler`（`@RestControllerAdvice` + `@Slf4j`）統一攔截所有例外
  - `UnauthorizedException` → 401（帳密錯誤不印 log，屬於預期內的業務情況）
  - `NoSuchElementException` → 404，`log.warn`
  - `IllegalArgumentException` → 400，`log.warn`
  - `RuntimeException` → 400，`log.warn`
  - `Exception`（兜底）→ 500，`log.error`
- `UnauthorizedException`：繼承 RuntimeException，不需要強制 try-catch，Spring 自動攔截

## 保單模組

### 保費試算
- `POST /client/policy/quote`：依年齡、性別、職業、旅遊天數、保額方案計算保費
- 試算不需要登入，降低使用門檻

### 線上投保
- `POST /client/policy/apply`：填寫被保人資料後送出投保申請
- 保單初始狀態為待簽核

### 個人資料管理
- `GET /client/profile`：查詢個人資料
- `PUT /client/profile`：修改個人資料（動態更新，只更新有傳值的欄位）

### 保單查詢與管理
- `GET /client/plans`：查詢個人保單列表
- `POST /client/plans/cancel`：取消保單（限 DRAFT、SIGNING 狀態）
- `GET /client/plans/download`：下載保單 PDF

## 簽核模組

### 簽核流程
- `POST /api/approval/submit`：業務員送審保單
- 簽核操作（核准/駁回）用 `@Transactional` 確保簽核紀錄寫入跟保單狀態更新綁成同一筆交易

### 工作清單
- `GET /api/approval/worklist`：業務員查看待處理保單清單
- `GET /api/approval/policies`：依角色（業務員/主管）查詢保單列表
- `GET /api/approval/policies/{policyId}`：查詢保單詳情

### 歷程紀錄
- `GET /api/approval/history/{policyId}`：查詢某張保單的完整簽核歷程

### 自動通知
- 簽核動作完成後，`afterCommit` 觸發非同步寄信（`CompletableFuture.runAsync`）
- 確保交易 commit 成功後才寄信，避免交易失敗但信已寄出的情況
- 寄信失敗用 `log.error` 記錄，不影響主流程

## 資料庫表格

| 資料表 | 說明 |
|---|---|
| `users` | 使用者資料（帳號、密碼雜湊、身分證、Email、角色、驗證狀態） |
| `policies` | 保單資料（投保人、被保人、旅遊日期、保費、保單狀態） |
| `coverage_amounts` | 保額方案資料 |
| `occupation_rates` | 職業費率表 |
| `mortality_rates` | 死亡率費率表 |
| `approval_log` | 簽核紀錄（業務員送審、主管核准/駁回歷程） |

## 啟動方式

```bash
git clone https://github.com/Alexlliu0421/travel-insurance-back.git
cd travel-insurance-back
./mvnw spring-boot:run
```

後端預設跑在 `http://localhost:8080`
Swagger UI：`http://localhost:8080/swagger-ui/index.html`

## 注意事項

- `pom.xml` 與 `application.yaml` 為共用設定檔，請勿直接 push，如需新增依賴或修改設定，請先在群組溝通，由負責人手動同步
- JWT 密鑰、過期秒數統一在 `application.yaml` 管理，請勿寫死在程式碼裡
- Mail 相關設定（SMTP、帳號、應用程式密碼）同樣在 `application.yaml` 管理
