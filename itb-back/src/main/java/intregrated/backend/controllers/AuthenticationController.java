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
            HttpServletResponse response) {

        // validate credentials (password checks etc.)
        UsersAccount user = authenticationService.validateCredentialsAndGetUser(request);

        // check remember token
        boolean hasRemember = otpService.hasValidRememberToken(user);
        if (hasRemember) {
            // generate tokens immediately and set cookie like before
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
        } else {
            // request OTP (this will enforce 60s cooldown etc.)
            RequestOtpResponseDto otpResp = otpService.requestOtp(user.getEmail());
            // return 202 Accepted with message (do not issue tokens)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(otpResp);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AccessTokenResponseDto> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request,
            HttpServletResponse response) {

        try {
            otpService.verifyOtp(request.getEmail(), request.getOtp(), Boolean.TRUE.equals(request.getRememberMe()));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AccessTokenResponseDto(null));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AccessTokenResponseDto(null));
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
