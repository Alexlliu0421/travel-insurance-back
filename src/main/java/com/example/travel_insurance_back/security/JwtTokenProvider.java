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

    // generateVerifyToken：產生「Email 驗證專用」的 JWT token
    // JWT（JSON Web Token）是一種把資料加密簽章後變成一串文字的格式
    // 拿這串文字本身就能驗證身份，不需要額外存 session 在後端，前端拿著它就能證明「我是誰」
    //
    // 跟登入用的 generateToken 不同：這裡不需要 role（角色），
    // 但多了一個 purpose claim，用來區分這個 token 是做什麼用的
    // 過期時間沿用跟登入 token 一樣的 expirationSeconds（目前是 24 小時）
    public String generateVerifyToken(Long userId) {
        return Jwts.builder()
                // claim：把資料塞進 token 裡（這些資料叫 claims，解析時可以讀出來）
                .claim("userId", userId)
                // 記錄這個 token 是「哪個使用者」的，之後驗證時要用這個 id 去資料庫查人
                .claim("purpose", "EMAIL_VERIFY")
                // 標記這個 token 的用途是「Email 驗證」
                // 系統裡還有別的 token（像重設密碼用的）格式長得一樣，
                // 沒有這個標記的話，使用者可能把驗證信連結誤用成重設密碼連結
                .setIssuedAt(new Date())
                // 記錄這個 token 是什麼時候產生的（發行時間）
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                // 設定過期時間：現在時間 + expirationSeconds
                // 超過這個時間，之後解析 token 會被判定為過期、視為無效
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                // 用後端自己保管的密鑰（jwtSecret），對整個 token 內容做數位簽章（HS256 演算法）
                // 確保沒有人能偽造或竄改這個 token，因為別人沒有這組密鑰，
                // 改了任何一個字，簽章驗證就會失敗
                .compact();
        // 把以上所有設定組合、編碼成最終的字串格式
        // 這串文字就是真正寄到信箱裡、放進連結裡的東西
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