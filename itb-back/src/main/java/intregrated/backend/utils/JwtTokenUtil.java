package intregrated.backend.utils;


import intregrated.backend.entities.accounts.UsersAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private static final String ISSUER = "http://intproj24.sit.kmutt.ac.th/sy4/";
    private static final long EXPIRATION_MS = 30*60*1000;
    private static final long verify_EXPIRATION_MS = 30*60*1000;
    private final Key key;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret) {
        // ตรวจสอบว่า secret key มีความยาวเพียงพอ (อย่างน้อย 32 characters สำหรับ HS256)
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateVerificationToken(UsersAccount user) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + verify_EXPIRATION_MS))
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .signWith(key)
                .compact();
    }

    public String generateToken(UsersAccount user, String role) {
    long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .claim("nickname", user.getNickname())
                .claim("typ", "access_token")
                .claim("id", user.getId())
                .claim("role", role)
                .claim("email", user.getEmail());

        if (user.getSeller() != null) {
            builder.claim("seller_id", user.getSeller().getId());
        }
        if (user.getBuyer() != null) {
            builder.claim("buyer_id", user.getBuyer().getId());
        }

        return builder.signWith(key).compact();
    }

    public String generateRefreshToken(UsersAccount user) {
        long now = System.currentTimeMillis();
        long refreshExpirationMs = 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpirationMs))
                .claim("typ", "refresh_token")
                .claim("id", user.getId())
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // เมธอดเสริมสำหรับ debug
    public void debugToken(String token) {
        try {
            Claims claims = getClaims(token);
            System.out.println("=== JWT TOKEN DEBUG ===");
            System.out.println("Issuer: " + claims.getIssuer());
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("Issued At: " + claims.getIssuedAt());
            System.out.println("Expiration: " + claims.getExpiration());
            System.out.println("User ID: " + claims.get("id"));
            System.out.println("Email: " + claims.get("email"));
            System.out.println("Is Expired: " + claims.getExpiration().before(new Date()));
            System.out.println("=======================");
        } catch (Exception e) {
            System.err.println("Cannot debug token: " + e.getMessage());
        }
    }
}
