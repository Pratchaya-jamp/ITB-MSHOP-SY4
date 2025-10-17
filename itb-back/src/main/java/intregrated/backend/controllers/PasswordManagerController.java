package intregrated.backend.controllers;

import intregrated.backend.dtos.password.NewPasswordRequest;
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

    @PostMapping("/change-password")
    public ResponseEntity<String> requestChangePassword(@RequestParam("email") String email) {
        passwordManageService.handleChangePasswordRequest(email);
        return ResponseEntity.ok("Password reset email sent successfully to " + email);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<UserRegisterResponseDto> verifyChangePasswordToken(@RequestParam("token") String jwtToken) {
        UserRegisterResponseDto response = passwordManageService.verifyEmailToken(jwtToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody NewPasswordRequest req
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        passwordManageService.resetPassword(
                token,
                req.getNewPassword(),
                req.getConfirmPassword()
        );

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
