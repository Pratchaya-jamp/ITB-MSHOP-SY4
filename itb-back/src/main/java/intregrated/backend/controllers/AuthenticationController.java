package intregrated.backend.controllers;

import intregrated.backend.dtos.authentications.*;
import intregrated.backend.dtos.authentications.RequestOtpResponseDto;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.services.AuthenticationService;
import intregrated.backend.services.OtpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/auth")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto request,
            @CookieValue(value = "remember_token", required = false) String rememberToken,
            HttpServletResponse response) {

        UsersAccount user = authenticationService.validateCredentialsAndGetUser(request);

        boolean hasRemember = false;
        if (rememberToken != null) {
            hasRemember = otpService.hasValidRememberToken(rememberToken, user);
        }

        if (hasRemember) {
            LoginResponseDto tokens = authenticationService.generateTokensForUser(user);

            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefresh_token())
                    .httpOnly(true)
                    .secure(true)
                    .path("/sy4")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader("Set-Cookie", refreshCookie.toString());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AccessTokenResponseDto(tokens.getAccess_token()));
        } else {
            RequestOtpResponseDto otpResp = otpService.requestOtp(user.getEmail());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(otpResp);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AccessTokenResponseDto> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request,
            HttpServletResponse response) {

        String rememberToken = null;
        try {
            rememberToken = otpService.verifyOtp(request.getEmail(), request.getOtp(), Boolean.TRUE.equals(request.getRememberMe()));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AccessTokenResponseDto(null));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AccessTokenResponseDto(null));
        }

        if (rememberToken != null) {
            ResponseCookie rememberCookie = ResponseCookie.from("remember_token", rememberToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/sy4")
                    .maxAge(30L * 24 * 60 * 60) // 30 วัน
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", rememberCookie.toString());
        } else if (Boolean.FALSE.equals(request.getRememberMe())) {
            // ถ้า User ไม่ติ๊ก (และ rememberToken เป็น null) ให้สั่งลบ Cookie เก่าทิ้ง
            ResponseCookie deleteRememberCookie = ResponseCookie.from("remember_token", "") // ตั้งค่าเป็นค่าว่าง
                    .httpOnly(true)
                    .secure(true)
                    .path("/sy4")
                    .maxAge(0)     // ตั้งค่า MaxAge = 0 เพื่อลบ
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", deleteRememberCookie.toString());
        }

        UsersAccount user = authenticationService.getUserByEmail(request.getEmail());
        LoginResponseDto tokens = authenticationService.generateTokensForUser(user);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefresh_token())
                .httpOnly(true)
                .secure(true)
                .path("/sy4")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccessTokenResponseDto(tokens.getAccess_token()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        authenticationService.revokeRefreshToken(refreshToken);

        // สร้าง HttpOnly Cookie สำหรับ Refresh Token
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/sy4")
                .maxAge(0)
                .sameSite("Strict") // Add SameSite here
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDto> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            System.out.println("No refresh token in cookie");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AccessTokenResponseDto(null));
        }

        LoginResponseDto newTokens = authenticationService.refreshAccessToken(refreshToken);

        // Replace the cookie with the new refresh token
        Cookie cookie = new Cookie("refresh_token", newTokens.getRefresh_token());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/sy4");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AccessTokenResponseDto(newTokens.getAccess_token()));
    }
}
