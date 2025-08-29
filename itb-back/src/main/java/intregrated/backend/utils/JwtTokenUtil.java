package intregrated.backend.utils;


import intregrated.backend.entities.UsersAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private static final String ISSUER = "http://intproj24.sit.kmutt.ac.th/sy4/";
    private static final long EXPIRATION_MS = 30*60*1000;
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(UsersAccount user, String role) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .claim("nickname", user.getNickname())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UsersAccount user) {
        long now = System.currentTimeMillis();
        long refreshExpirationMs = 7 * 24 * 60 * 60 * 1000; // 7 days

        return Jwts.builder()
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpirationMs))
                .claim("id", user.getId())
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
