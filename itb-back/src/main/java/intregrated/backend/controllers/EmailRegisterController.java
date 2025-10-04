package intregrated.backend.controllers;

import intregrated.backend.dtos.registers.UserRegisterRequestDto;
import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.services.EmailRegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/auth")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class EmailRegisterController {
    @Autowired
    private EmailRegisterService emailRegisterService;

    @PostMapping("/verify-email")
    public ResponseEntity<UserRegisterResponseDto> verifyEmail(@RequestParam("token") String jwtToken) {
            UserRegisterResponseDto response = emailRegisterService.verifyEmailToken(jwtToken);
            return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserRegisterResponseDto> registerUsers(
            @Valid @ModelAttribute UserRegisterRequestDto request,
            @RequestParam(value = "idCardImageFront", required = false) MultipartFile idCardImageFront,
            @RequestParam(value = "idCardImageBack", required = false) MultipartFile idCardImageBack
    ) {
            if ("BUYER".equalsIgnoreCase(request.getUserType())) {
                UserRegisterResponseDto account = emailRegisterService.registerBuyer(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(account);
            } else if ("SELLER".equalsIgnoreCase(request.getUserType())) {
                UserRegisterResponseDto account = emailRegisterService.registerSeller(request, idCardImageFront, idCardImageBack);
                return ResponseEntity.status(HttpStatus.CREATED).body(account);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userType, must be BUYER or SELLER");
            }
    }
}
