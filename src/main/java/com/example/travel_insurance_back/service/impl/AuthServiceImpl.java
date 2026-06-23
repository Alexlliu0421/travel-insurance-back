package com.example.travel_insurance_back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.travel_insurance_back.dto.request.ForgotPasswordReqDTO;
import com.example.travel_insurance_back.dto.request.LoginReqDTO;
import com.example.travel_insurance_back.dto.request.RegisterReqDTO;
import com.example.travel_insurance_back.dto.request.ResetPasswordReqDTO;
import com.example.travel_insurance_back.dto.response.LoginRespDTO;
import com.example.travel_insurance_back.entity.User;
import com.example.travel_insurance_back.mapper.UserMapper;
import com.example.travel_insurance_back.security.JwtTokenProvider;
import com.example.travel_insurance_back.service.AuthService;
import com.example.travel_insurance_back.service.EmailService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. 用 email 查詢 user
    // 2. 檢查 user 是否存在
    // 3. 檢查密碼是否正確（passwordEncoder.matches）
    // 4. 檢查 is_verified 是否為 true
    // 5. 產生 token
    // 6. 回傳 LoginRespDTO 物件

    @Override
    public LoginRespDTO login(LoginReqDTO loginReqDTO) {
        User user = userMapper.findByIdNumber(loginReqDTO.getIdNumber());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(loginReqDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        if (!user.getIsVerified()) {
            throw new RuntimeException("Email not verified");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        LoginRespDTO resp = new LoginRespDTO();
        resp.setUserId(user.getId());
        resp.setName(user.getName());
        resp.setRole(user.getRole());
        resp.setToken(token);
        return resp;
    }

    @Override
    // register：處理會員註冊的完整流程
    // 1. 檢查 email、身分證字號是否已被註冊（雙重防呆，避免重複帳號）
    // 2. 密碼加密（用 BCrypt，絕不存明文密碼）
    // 3. 建立 User 物件，把所有欄位填好，狀態先設成「未驗證」存進資料庫
    // 4. 產生一個專屬的驗證用 JWT token
    // 5. 寄送驗證信給使用者填的 Email
    public void register(RegisterReqDTO registerReqDTO) {

        // 用 email 查資料庫，如果已經有人用過這個 email 就拋例外擋掉
        User existingUser = userMapper.findByEmail(registerReqDTO.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("Email already exists");
        }

        // 同樣的邏輯查身分證字號，避免同一個人或盜用他人身分證重複註冊
        User existingIdNumber = userMapper.findByIdNumber(registerReqDTO.getIdNumber());
        if (existingIdNumber != null) {
            throw new RuntimeException("身分證字號已被註冊");
        }

        // 把使用者輸入的明文密碼加密，BCrypt 是單向加密，無法逆向還原回原始密碼
        // 之後登入時是用 passwordEncoder.matches() 拿輸入的密碼跟這串加密結果比對，不是解密
        String encodedPassword = passwordEncoder.encode(registerReqDTO.getPassword());

        User user = new User();
        user.setEmail(registerReqDTO.getEmail());
        user.setPassword(encodedPassword);

        // 關鍵：剛註冊的帳號狀態先設成「未驗證」，要等使用者點驗證信連結才會變成 true
        // 在這個狀態下，即使密碼輸對也無法登入（login() 那邊有擋這個判斷）
        user.setIsVerified(false);

        // 把表單上其他欄位依序填進 User 物件
        user.setName(registerReqDTO.getName());
        user.setIdNumber(registerReqDTO.getIdNumber());
        user.setPhone(registerReqDTO.getPhone());
        user.setAddress(registerReqDTO.getAddress());
        user.setRole("USER"); // 一般會員的預設角色
        user.setStatus("ACTIVE");
        user.setBirthDate(registerReqDTO.getBirthDate());
        user.setNationality(registerReqDTO.getNationality());
        user.setGender(registerReqDTO.getGender());
        user.setOccupationName(registerReqDTO.getOccupationName());

        // 存進資料庫；insert 完成後，MyBatis 會自動把資料庫產生的自增 id 回填到 user.getId()
        userMapper.insert(user);

        // 拿到剛存進去的 user id，產生一個專屬於這個使用者、用途是「Email驗證」的 token
        String verifyToken = jwtTokenProvider.generateVerifyToken(user.getId());

        // 把這個 token 包進驗證連結，寄到使用者填的 email
        emailService.sendVerificationEmail(user.getEmail(), verifyToken);
    }

    @Override
    // verifyEmail：使用者點擊驗證信連結後，後端要做的事
    // 1. 確認 token 本身有效（沒過期、簽章正確）
    // 2. 從 token 解出 userId，去資料庫查這個人是否存在
    // 3. 確認這個帳號還沒驗證過（避免重複觸發）
    // 4. 把帳號狀態改成已驗證
    public void verifyEmail(String token) {

        // validateToken 內部其實是「嘗試解析 token，成功代表有效，
        // 解析失敗（比如過期、簽章不對）會丟例外，被接住變成回傳 false」
        boolean isValid = jwtTokenProvider.validateToken(token);
        if (!isValid) {
            throw new RuntimeException("Token 已過期或無效");
        }

        // 從 token 裡解出這個 token 是「幫哪個使用者」產生的
        Long userId = jwtTokenProvider.getUserId(token);

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("使用者不存在");
        }

        // 防止使用者重複點同一個連結，已經驗證過就直接擋掉，不用重複處理
        if (user.getIsVerified()) {
            throw new RuntimeException("此帳號已驗證過");
        }

        // 把資料庫裡這個使用者的 is_verified 改成 true，並清掉 verify_token
        userMapper.verifyEmail(user.getId());
    }

    @Override
    // 忘記密碼：使用者輸入 email，後端寄送重設密碼信
    //
    // 安全性考量：不管這個 email 有沒有註冊過，都不告訴前端「有」或「沒有」，
    // 統一回應成功（前端畫面看起來都一樣），避免被用來測試/枚舉哪些 email 已經註冊
    // （這個攻擊手法叫做「帳號枚舉攻擊」，細節可參考之前的討論）
    public void forgotPassword(ForgotPasswordReqDTO forgotPasswordReqDTO) {

        // 用 email 查詢使用者，這裡可能查到 user，也可能查不到（user 是 null）
        User user = userMapper.findByEmail(forgotPasswordReqDTO.getEmail());

        // 只有「真的查到使用者」才會執行寄信的動作
        // 如果 user 是 null（這個 email 沒人註冊過），這個 if 區塊不會執行，
        // 但因為這個方法本身沒有 return 任何東西、也沒有 throw 例外，
        // 所以從外部呼叫端的角度看，「查到」跟「沒查到」的結果完全一樣（都正常結束、沒有錯誤訊息）
        if (user != null) {

            // 產生一個專屬於這個使用者、用途是「重設密碼」的 token
            // （跟 Email 驗證用的 token 是不同方法產生的，purpose 欄位也不同）
            String token = jwtTokenProvider.generateResetPasswordToken(user.getId());

            // 把附帶這個 token 的「重設密碼連結」寄到使用者的 email
            emailService.sendResetPasswordEmail(user.getEmail(), token);
        }

        // 不管上面 if 有沒有執行，這個方法都會正常結束（沒有回傳值、沒有拋例外）
        // Controller 那邊收到這個方法執行完畢後，會統一回傳同一種「成功」訊息給前端
    }

    @Override
    // 重設密碼：使用者點擊信件連結、填寫新密碼後送出，帶著 token + 新密碼呼叫這個方法
    public void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO) {

        String token = resetPasswordReqDTO.getToken();

        // Step 1：先確認 token 本身有效（格式對、簽章正確、沒有過期）
        // validateToken 內部其實是「嘗試解析 token，解析成功代表有效，解析失敗（例如過期）會拋例外被接住變成 false」
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Token 已過期或無效");
        }

        // Step 2：確認這個 token 的用途真的是「重設密碼」，不是「Email 驗證」的 token被拿來亂用
        // 因為兩種 token 的產生方式只差在 purpose 這個欄位，格式上都是合法的 JWT，
        // 如果不檢查 purpose，使用者可能會把驗證信的連結貼來當重設密碼連結用（雖然影響不大，但邏輯上不該允許）
        String purpose = jwtTokenProvider.getPurpose(token);
        if (!"RESET_PASSWORD".equals(purpose)) {
            throw new RuntimeException("Token 用途不正確");
        }

        // Step 3：從 token 裡解出這個 token 是「幫哪個使用者」產生的
        Long userId = jwtTokenProvider.getUserId(token);

        // Step 4：去資料庫確認這個使用者真的存在
        // （理論上只要 token 是我們自己產生、且還沒過期，使用者應該都存在，
        // 但還是要防範使用者在這段時間內被刪除等極端情況）
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("使用者不存在");
        }

        // Step 5：把使用者輸入的新密碼加密（跟註冊時用同一套加密方式 BCrypt），
        // 絕對不能把明文密碼直接存進資料庫
        String encodedPassword = passwordEncoder.encode(resetPasswordReqDTO.getNewPassword());

        // Step 6：更新資料庫裡這個使用者的密碼欄位
        userMapper.updatePassword(user.getId(), encodedPassword);

    }

    @Override
    // 檢查 email 是否已存在：直接查資料庫，回傳 true/false
    // 用於註冊頁 Step2，使用者填完 email 欄位後即時呼叫，不用等到 Step3 送出才知道
    public boolean checkEmailExists(String email) {
        return userMapper.findByEmail(email) != null;
    }

    @Override
    // 檢查身分證字號是否已存在：邏輯跟上面一樣，只是換查身分證字號
    public boolean checkIdNumberExists(String idNumber) {
        return userMapper.findByIdNumber(idNumber) != null;
    }
}