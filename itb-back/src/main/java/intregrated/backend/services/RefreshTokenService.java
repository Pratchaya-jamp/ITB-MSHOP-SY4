package intregrated.backend.services;

import intregrated.backend.entities.RefreshToken;
import intregrated.backend.entities.UsersAccount;
import intregrated.backend.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(UsersAccount user, String token, long expirySeconds) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(expirySeconds));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedOn(Instant.now());
        refreshToken.setUpdatedOn(Instant.now());
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            rt.setUpdatedOn(Instant.now());
            refreshTokenRepository.save(rt);
        });
    }

    public boolean isValid(RefreshToken token) {
        return !token.getRevoked() && token.getExpiryDate().isAfter(Instant.now());
    }
}