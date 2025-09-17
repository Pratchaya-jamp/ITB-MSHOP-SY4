package intregrated.backend.services;

import intregrated.backend.dtos.authentications.LoginRequestDto;
import intregrated.backend.dtos.authentications.LoginResponseDto;
import intregrated.backend.entities.UsersAccount;
import intregrated.backend.repositories.UsersAccountRepository;
import intregrated.backend.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthenticationService {

    @Autowired
    private UsersAccountRepository usersRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // เก็บ revoked tokens (จริง ๆ ควรใช้ DB/Redis)
    private final Set<String> revokedRefreshTokens = new HashSet<>();

    public LoginResponseDto authenticateUser(LoginRequestDto requestDto) {
        // Null values → 400
        if (requestDto.getEmail() == null || requestDto.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
        }

        if (requestDto.getEmail().length() > 50) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
        }

        if (requestDto.getPassword().length() < 8 || requestDto.getPassword().length() > 14) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
        }

        // Empty string or only spaces → 401
        if (requestDto.getEmail().trim().isEmpty() || requestDto.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
        }

        // Leading/trailing spaces → 401
        if (!requestDto.getEmail().equals(requestDto.getEmail().trim()) ||
                !requestDto.getPassword().equals(requestDto.getPassword().trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
        }

        Optional<UsersAccount> optionalUser = usersRepo.findByEmail(requestDto.getEmail());

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or Password is incorrect.");
        }

        UsersAccount userAccount = optionalUser.get();

        if (!Boolean.TRUE.equals(userAccount.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You need to activate your account before signing in.");
        }

        if (!passwordEncoder.matches(requestDto.getPassword(), userAccount.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Email or Password is incorrect.");
        }

        String role = getRole(userAccount);

        String accessToken = jwtTokenUtil.generateToken(userAccount, role);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userAccount);

        return LoginResponseDto.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .build();
    }

    public LoginResponseDto refreshAccessToken(String refreshToken) {
        if (revokedRefreshTokens.contains(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Token");
        }

        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Token");
        }

        Claims claims = jwtTokenUtil.getClaims(refreshToken);
        Integer userId = claims.get("id", Integer.class);

        UsersAccount user = usersRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not active");
        }

        String role = getRole(user);

        String newAccessToken = jwtTokenUtil.generateToken(user, role);

        return LoginResponseDto.builder()
                .access_token(newAccessToken)
                .build(); // return access token only
    }

    public void revokeRefreshToken(String refreshToken) {
        revokedRefreshTokens.add(refreshToken);
    }

    public String getRole(UsersAccount userAccount) {
        if (userAccount.getSeller() != null) {
            return "SELLER";
        } else if (userAccount.getBuyer() != null) {
            return "BUYER";
        }
        return "USER";
    }
}
