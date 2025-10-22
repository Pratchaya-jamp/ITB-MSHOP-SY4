package intregrated.backend.controllers;

import intregrated.backend.dtos.authentications.AccessTokenResponseDto;
import intregrated.backend.dtos.authentications.LoginRequestDto;
import intregrated.backend.dtos.authentications.LoginResponseDto;
import intregrated.backend.services.AuthenticationService;
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

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        LoginResponseDto tokens = authenticationService.authenticateUser(request);

        // สร้าง HttpOnly Cookie สำหรับ Refresh Token
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefresh_token())
                .httpOnly(true)
                .secure(true)
                .path("/sy4")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict") // Add SameSite here
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // return access_token เท่านั้นใน body
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
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null) {
            System.out.println("No refresh token in cookie");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AccessTokenResponseDto(null));
        }

        LoginResponseDto response = authenticationService.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccessTokenResponseDto(response.getAccess_token()));
    }
}
