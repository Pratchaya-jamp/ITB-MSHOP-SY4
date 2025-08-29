package intregrated.backend.services;

import intregrated.backend.dtos.authentications.MatchPasswordRequestDto;
import intregrated.backend.dtos.authentications.MatchpasswordResponseDto;
import intregrated.backend.entities.UsersAccount;
import intregrated.backend.repositories.UsersAccountRepository;
import intregrated.backend.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private UsersAccountRepository usersRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;



    public MatchpasswordResponseDto authenticateUser(MatchPasswordRequestDto requestDto) {
        Optional<UsersAccount> optionalUser = usersRepo.findByEmail(requestDto.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Username or Password is incorrect.");
        }

        UsersAccount userAccount = optionalUser.get();

        if (!passwordEncoder.matches(requestDto.getPassword(), userAccount.getPassword())) {
            throw new RuntimeException("Username or Password is incorrect.");
        }

        String role = "USER";
        if (userAccount.getSeller() != null) {
            role = "SELLER";
        } else if (userAccount.getBuyer() != null) {
            role = "BUYER";
        }

        String accessToken = jwtTokenUtil.generateToken(userAccount, role);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userAccount);

        return new MatchpasswordResponseDto(accessToken, refreshToken);
    }
}
