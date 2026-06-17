package com.example.travel_insurance_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Swagger 設定檔，開發測試用
// 讓 Swagger UI 出現 🔒 Authorize 按鈕
// 可輸入 JWT token 測試需要登入的 API
// 網址：http://localhost:8080/swagger-ui/index.html
@Configuration
public class SwaggerConfig {

    @Bean
    // OpenAPI 設定
    // 所有 API 預設都需要 Bearer token 驗證
    // Bearer → HTTP 標準的 token 驗證方式
    // bearerFormat JWT → 指定 token 格式為 JWT
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        ));
    }
}