package intregrated.backend.controllers;

import intregrated.backend.dtos.UserRegisterRequestDto;
import intregrated.backend.dtos.UserRegisterResponseDto;
import intregrated.backend.entities.UsersAccount;
import intregrated.backend.services.EmailRegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v2/users")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class EmailRegisterController {
    @Autowired
    private EmailRegisterService emailRegisterService;

    @GetMapping("")
    private ResponseEntity<List<UsersAccount>> getAllUsersAccounts() {
        List<UsersAccount> accounts = emailRegisterService.getAllUsers();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUsers(
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
                return ResponseEntity.badRequest().body("Invalid userType, must be BUYER or SELLER");
            }
    }

    @DeleteMapping("/{uid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer uid) {
        emailRegisterService.deleteUser(uid);
    }
}
