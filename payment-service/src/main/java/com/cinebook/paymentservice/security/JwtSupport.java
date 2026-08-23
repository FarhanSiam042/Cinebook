package com.cinebook.paymentservice.security;

import com.cinebook.paymentservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JwtSupport {

    private final JwtProperties properties;

    public JwtSupport(JwtProperties properties) {
        this.properties = properties;
    }

    public boolean isTokenValid(String token) {
        try {
            return parseClaims(token).getExpiration().after(new Date());
        }
        catch (Exception exception) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }
}
