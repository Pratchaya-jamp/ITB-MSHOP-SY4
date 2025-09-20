package intregrated.backend.controllers;

import intregrated.backend.dtos.authentications.AccessTokenResponseDto;
import intregrated.backend.dtos.authentications.LoginRequestDto;
import intregrated.backend.dtos.authentications.LoginResponseDto;
import intregrated.backend.services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/auth")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

//    @PostMapping("/login")
//    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
//        LoginResponseDto response = authenticationService.authenticateUser(request);
//        return ResponseEntity.ok().body(response);
//    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        LoginResponseDto tokens = authenticationService.authenticateUser(request);

        // สร้าง HttpOnly Cookie สำหรับ Refresh Token
        Cookie refreshCookie = new Cookie("refresh_token", tokens.getRefresh_token());
        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(true); // ควรเปิดบน production (https)
        refreshCookie.setPath("/sy4/itb-mshop/v2/auth");
        refreshCookie.setMaxAge(24 * 60 * 60); // 1 วัน
        response.addCookie(refreshCookie);

        // return access_token เท่านั้นใน body
        return ResponseEntity.ok(new AccessTokenResponseDto(tokens.getAccess_token()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        authenticationService.revokeRefreshToken(refreshToken);

        // Remove cookie
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        cookie.setPath("/sy4/itb-mshop/v2/auth/refresh");
        cookie.setMaxAge(0); // expire now
        response.addCookie(cookie);

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

        System.out.println("Refresh token received: " + refreshToken);

        LoginResponseDto response = authenticationService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new AccessTokenResponseDto(response.getAccess_token()));
    }
}
