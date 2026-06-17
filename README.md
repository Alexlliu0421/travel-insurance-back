# Travel Insurance Back

旅遊險專案後端，使用 Spring Boot + MyBatis + MySQL 開發。

## 環境需求

- Java 17
- Maven 3.x
- MySQL 8.x

## 安裝步驟

```bash
# 1. clone 專案
git clone https://github.com/Alexlliu0421/travel-insurance-back.git

# 2. 進入資料夾
cd travel-insurance-back

# 3. 建立資料庫
CREATE DATABASE travel_insurance_db;

# 4. 設定資料庫（修改 src/main/resources/application.yml）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/travel_insurance_db
    username: 你的帳號
    password: 你的密碼

# 5. 啟動專案
./mvnw spring-boot:run
```

## 專案結構

```
src/main/java/com/example/travel_insurance_back/
├── common/       # 統一回傳格式 ApiResponse、ResultCode
├── config/       # SecurityConfig、SwaggerConfig
├── controller/   # API 路由
├── dto/          # 資料傳輸物件
├── entity/       # 資料模型
├── exception/    # 全域例外處理
├── mapper/       # MyBatis Mapper
├── security/     # JWT 驗證
└── service/      # 業務邏輯
```

## 主要套件

- Spring Boot 3.5
- Spring Security + JWT (jjwt 0.11.5)
- MyBatis
- MySQL Driver
- Lombok
- Swagger (springdoc-openapi 2.8.9)

## Swagger UI

啟動後可至以下網址測試 API：
http://localhost:8080/swagger-ui/index.html