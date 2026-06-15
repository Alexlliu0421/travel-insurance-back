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

# 3. 設定資料庫（修改 src/main/resources/application.yml）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/travel_insurance
    username: 你的帳號
    password: 你的密碼

# 4. 啟動專案
./mvnw spring-boot:run
```

## 專案結構

```
src/main/java/com/example/
├── controller/   # API 路由
├── service/      # 業務邏輯
├── mapper/       # MyBatis Mapper
├── model/        # 資料模型
└── config/       # 設定檔
```

## 主要套件

- Spring Boot 3.5
- Spring Security
- MyBatis
- MySQL Driver
- Lombok