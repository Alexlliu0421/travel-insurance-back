package com.example.travel_insurance_back.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-seconds}")
    private long expirationSeconds;

    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 讀取 token 裡的 purpose claim，確認這個 token 是用於哪種用途
    // （EMAIL_VERIFY 或 RESET_PASSWORD），避免兩種功能的 token 互相誤用
    public String getPurpose(String token) {
        return getClaims(token).get("purpose", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // 在 JwtTokenProvider.java 裡加這個方法

    // 產生 Email 驗證專用 token
    // 跟登入 token 不同，這裡不需要 role，但要加一個 purpose claim 區分用途
    // 過期時間可以跟登入 token 共用 expirationSeconds，或自訂更短的天數
    public String generateVerifyToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("purpose", "EMAIL_VERIFY")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    // 產生重設密碼專用 token
    // 跟 generateVerifyToken 邏輯幾乎一樣，只是 purpose 不同，用來區分這個 token 是「拿來重設密碼」
    // 而不是「拿來驗證 Email」，避免使用者把驗證信連結誤用成重設密碼連結（反之也是）
    public String generateResetPasswordToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId) // 把使用者 id 包進 token，之後驗證時可以解出來知道是誰要重設密碼
                .claim("purpose", "RESET_PASSWORD") // 標記這個 token 的用途，跟 EMAIL_VERIFY 區分
                .setIssuedAt(new Date()) // 記錄 token 產生的時間
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                // 設定過期時間：現在時間 + expirationSeconds（從 application.yaml 讀進來的設定，目前是 24 小時）
                // 超過這個時間，token 解析時會被判定為過期、視為無效
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                // 用密鑰（jwtSecret）對整個 token 做數位簽章，確保沒有人能偽造或竄改這個 token
                // 別人沒有這個密鑰，就算想自己做一個假 token 也會被識破
                .compact();
        // 把以上設定組合、編碼成最終的字串格式（就是真正寄出去的那串文字）
    }
}