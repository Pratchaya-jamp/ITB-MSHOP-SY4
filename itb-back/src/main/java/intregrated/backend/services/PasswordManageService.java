package intregrated.backend.services;

import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.repositories.accounts.UsersAccountRepo;
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
import java.util.Objects;
import java.util.UUID;

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

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Transactional
    public UserRegisterResponseDto verifyEmailToken(String jwtToken) {
        if (!jwtTokenUtil.validateToken(jwtToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(jwtToken);
        Integer userId = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);
        String saltFromToken = claims.get("salt", String.class);

        UsersAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token email mismatch");
        }

        if (!Objects.equals(user.getToken_used(), saltFromToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This link has already been used or expired");
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
    public void resetForgotPassword(String token, String newPassword, String confirmPassword) {
        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(token);
        Integer userId = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);
        String saltFromToken = claims.get("salt", String.class);

        UsersAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token email mismatch");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as old password");
        }

        if (!Objects.equals(user.getToken_used(), saltFromToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "This link has already been used or expired");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setUpdatedOn(Instant.now());

        user.setToken_used(UUID.randomUUID().toString());

        userRepo.saveAndFlush(user);

        sendNotifyEmail(email);
    }

    @Transactional
    public void resetChangePassword(String token, Integer uid, String oldPassword, String newPassword, String confirmPassword) {
        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(token);
        String emailFromToken = claims.get("email", String.class);
        Integer userIdFromToken = claims.get("id", Integer.class);

        if (emailFromToken == null || userIdFromToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User id cannot be null");
        }

        UsersAccount user = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!userIdFromToken.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id in access token not matched with resource id");
        }

        if (!user.getEmail().equals(emailFromToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token email mismatch");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from old password");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setUpdatedOn(Instant.now());

        userRepo.saveAndFlush(user);

        sendNotifyEmail(emailFromToken);
    }

    @Transactional
    public void handleChangePasswordRequest(String email) {
        UsersAccount user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newSalt = UUID.randomUUID().toString();
        user.setToken_used(newSalt);
        userRepo.saveAndFlush(user);

        String token = jwtTokenUtil.generateVerificationPasswordToken(user, newSalt);

        mailService.sendVerificationPassword(email, token);
    }

    private void sendNotifyEmail(String email) {
        mailService.sendPasswordNotify(email);
    }
}
