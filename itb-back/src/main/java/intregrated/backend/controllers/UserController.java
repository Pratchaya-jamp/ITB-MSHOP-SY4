package intregrated.backend.controllers;

import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.dtos.users.UserResponseDto;
import intregrated.backend.services.UserBaseService;
import intregrated.backend.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
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
    private ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // ไม่มี token → 401 Unauthorized
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // ตัด "Bearer " ออก

        // ตรวจสอบ token
        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        // ดึง claims จาก token
        Claims claims = jwtTokenUtil.getClaims(token);
        Integer tokenUserId = claims.get("id", Integer.class);

        // ถ้า id ใน path ไม่ตรงกับ id ใน token → 403 Forbidden
        if (!id.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request userId not matched with token userId");
        }

        // ดึง user จาก database
        UserResponseDto user = userBaseService.getUserById(id);

        if (user == null) {
            throw new  ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        return ResponseEntity.ok(user);
    }
}
