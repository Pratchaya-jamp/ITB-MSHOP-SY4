package intregrated.backend.services;

import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.repositories.UsersAccountRepo;
import intregrated.backend.utils.JwtTokenUtil;
import intregrated.backend.utils.UserTypeResolver;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class PasswordManageService {

    @Autowired
    private MailService mailService;

    @Autowired
    private JwtTokenUtil  jwtTokenUtil;

    @Autowired
    private UsersAccountRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserRegisterResponseDto verifyEmailToken(String jwtToken) {
        if (!jwtTokenUtil.validateToken(jwtToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(jwtToken);
        Integer userId = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);

        UsersAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token email mismatch");
        }

        return UserRegisterResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .isActive(user.getIsActive())
                .userType(UserTypeResolver.resolveUserType(user))
                .build();
    }

    @Transactional
    public void resetPassword(String jwtToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (!jwtTokenUtil.validateToken(jwtToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(jwtToken);
        Integer userId = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);

        UsersAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token email mismatch");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setUpdatedOn(Instant.now());
        userRepo.saveAndFlush(user);
    }

    @Transactional
    public void handleChangePasswordRequest(String email) {
        UsersAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String token = jwtTokenUtil.generateVerificationToken(user);

        mailService.sendVerificationPassword(email, token);
    }
}
