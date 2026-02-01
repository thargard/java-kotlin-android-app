package example.com.server.service;

import example.com.server.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String secret = "your-secret-key-your-secret-key-your-secret-key-your-secret-key-your-secret-key-your-secret-key";

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail() != null ? user.getEmail() : "")
                .claim("id", user.getId())
                .claim("login", user.getLogin())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    /**
     * Parse Bearer token and return user id from claims.
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public Long getUserIdFromToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        String token = bearerToken.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object id = claims.get("id");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        return null;
    }
}
