package intregrated.backend.services;

import intregrated.backend.entities.RefreshToken;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.repositories.RefreshTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    public RefreshToken createRefreshToken(UsersAccount user, String token, long expirySeconds) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(expirySeconds));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedOn(Instant.now());
        refreshToken.setUpdatedOn(Instant.now());
        return refreshTokenRepo.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public void revokeToken(String token) {
        refreshTokenRepo.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            rt.setUpdatedOn(Instant.now());
            refreshTokenRepo.save(rt);
        });
    }

    public boolean isValid(RefreshToken token) {
        return !token.getRevoked() && token.getExpiryDate().isAfter(Instant.now());
    }
}