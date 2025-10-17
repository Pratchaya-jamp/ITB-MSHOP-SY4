package intregrated.backend.controllers;

import intregrated.backend.dtos.registers.UserRegisterRequestDto;
import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.dtos.users.EditUserRequestDto;
import intregrated.backend.dtos.users.UserResponseDto;
import intregrated.backend.services.UserBaseService;
import intregrated.backend.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class UserController {

    @Autowired
    private UserBaseService userBaseService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/users")
    private ResponseEntity<List<UserRegisterResponseDto>> getAllUsersAccounts() {
        return ResponseEntity.ok(userBaseService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        UserResponseDto user = userBaseService.getUserById(id, token);
        return ResponseEntity.ok(user);
    }


    @PutMapping("/users/{id}")
    private ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false ) String authHeader,
            @Valid @RequestBody EditUserRequestDto userUpdateRequest
            )
    {
        // ไม่มี token → 401 Unauthorized
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // ตัด "Bearer " ออก

        UserResponseDto userUpdated = userBaseService.editUser(id, userUpdateRequest, token);

        return ResponseEntity.ok(userUpdated);
    }
}
