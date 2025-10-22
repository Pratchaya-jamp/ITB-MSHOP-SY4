package intregrated.backend.controllers;

import intregrated.backend.dtos.password.NewChangePasswordRequest;
import intregrated.backend.dtos.password.NewForgotPasswordRequest;
import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.services.PasswordManageService;
import intregrated.backend.utils.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class PasswordManagerController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordManageService  passwordManageService;

    @PostMapping("/send-email")
    public ResponseEntity<Void> requestChangePassword(@RequestParam("email") String email) {
        passwordManageService.handleChangePasswordRequest(email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify-password")
    public ResponseEntity<UserRegisterResponseDto> verifyChangePasswordToken(@RequestParam("token") String jwtToken) {
        UserRegisterResponseDto response = passwordManageService.verifyEmailToken(jwtToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/reset-password-forgot")
    public ResponseEntity<Void> resetForgotPassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody NewForgotPasswordRequest req

    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        passwordManageService.resetForgotPassword(
                token,
                req.getNewPassword(),
                req.getConfirmPassword()
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("user/{uid}/reset-password-change")
    public ResponseEntity<Void> resetChangePassword(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer uid,
            @Valid @RequestBody NewChangePasswordRequest req
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        passwordManageService.resetChangePassword(
                token,
                uid,
                req.getOldPassword(),
                req.getNewPassword(),
                req.getConfirmPassword()
        );

        return ResponseEntity.ok().build();
    }
}