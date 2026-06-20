## Email 驗證流程

1. 使用者註冊後，系統寄送一封包含驗證連結的 Email
2. 驗證連結使用 JWT 產生，內含 24 小時的過期時間（不需額外的資料庫欄位）
3. 點擊連結後，後端會顯示簡單的 HTML 結果頁面：
   - 驗證成功
   - Token 已過期或無效
   - 此帳號已驗證過
4. 驗證成功後，使用者才能使用 `/api/auth/login` 登入

## 注意事項

- `pom.xml` 與 `application.yaml` 為共用設定檔，請勿直接 push，如需新增依賴或修改設定，請先在群組溝通，由負責人手動同步
- 本次新增依賴：`spring-boot-starter-validation`（用於 `@Email` 格式驗證），請手動加入你的 `pom.xml`：

\`\`\`xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
\`\`\`