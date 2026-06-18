package com.example.travel_insurance_back.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.travel_insurance_back.security.JwtAuthenticationFilter;

// Spring Security 設定檔
// 負責兩件事：
// 1. 規定哪些 API 需要登入（需要 JWT token）、哪些可以直接存取
// 2. 設定 CORS，允許前端（localhost:5173）跨來源存取後端（localhost:8080）
@Configuration
// @Configuration → 告訴 Spring 這個 class 有 @Bean 方法，啟動時來這裡建立物件放進容器
@EnableWebSecurity // 啟用 Spring Security，讓 SecurityFilterChain 設定生效
public class SecurityConfig {

        // @Autowired → Spring 自動從容器找到 JwtAuthenticationFilter 注入
        // 省略了自己 new 物件的過程：
        // JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(); （還要手動讀
        // application.yml）
        // JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        // filter.setJwtTokenProvider(jwtTokenProvider);
        // Spring 容器統一管理：建立物件、注入依賴、讀取設定檔，一個 @Autowired 搞定
        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // CORS → 套用下面 corsConfigurationSource() 的設定

                                .csrf(csrf -> csrf.disable())
                                // 關閉 CSRF 保護 → 前後端分離用 JWT 驗證，不需要 CSRF token

                                .authorizeHttpRequests(auth -> auth
                                                // 你的 API - 不需要登入
                                                .requestMatchers("/api/auth/**").permitAll()
                                                // 保費試算 - 不需要登入（給未登入首頁用）
                                                .requestMatchers("/client/policy/quote").permitAll()
                                                // Swagger
                                                .requestMatchers("/swagger-ui/**").permitAll()
                                                // swagger測試用
                                                .requestMatchers("/v3/api-docs/**").permitAll()

                                                // 其他全部需要登入
                                                .anyRequest().authenticated())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                // 不使用 Session，每次請求都靠 JWT token 驗證身份
                                // 前後端分離的標準做法
                                );

                // 把 JwtAuthenticationFilter 插在 Spring Security 預設的登入 Filter 之前
                // 確保每個請求先過 JWT 驗證，再做後續的權限檢查
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
                // 把所有設定打包成 SecurityFilterChain，Spring 套用到每個進來的請求
        }

        @Bean
        // corsConfigurationSource() → 定義 CORS
        // 規則，允許前端（localhost:5173）跨來源存取後端（localhost:8080）
        // 前後端不同 port 就是不同來源，瀏覽器預設會擋跨來源請求，需要後端明確允許才能通過
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration config = new CorsConfiguration();

                // 規則一：誰可以來？只允許前端 5173，其他來源的請求瀏覽器會擋掉
                config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

                // 規則二：可以用什麼方法？
                config.setAllowedMethods(Arrays.asList(
                                HttpMethod.GET.name(),
                                HttpMethod.POST.name(),
                                HttpMethod.PUT.name(),
                                HttpMethod.DELETE.name(),
                                HttpMethod.OPTIONS.name() // OPTIONS → 瀏覽器發出跨來源請求前的預檢請求
                ));

                // 規則三：可以帶什麼 Header？允許所有 Header（包含 Authorization，才能帶 JWT token）
                config.setAllowedHeaders(Arrays.asList("*"));

                // 允許帶 Cookie（我們用 JWT token + localStorage，實際上用不到，保留設定）
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}