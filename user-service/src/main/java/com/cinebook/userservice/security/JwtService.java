package com.cinebook.userservice.security;

import com.cinebook.userservice.config.JwtProperties;
import com.cinebook.userservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final JwtProperties properties;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
	}

	public String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", user.getRoles().stream().map(Enum::name).toList());
		claims.put("fullName", user.getFullName());
		return Jwts.builder()
			.claims(claims)
			.subject(user.getUsername())
			.issuedAt(new Date())
			.expiration(Date.from(Instant.now().plus(properties.expirationMinutes(), ChronoUnit.MINUTES)))
			.signWith(getSigningKey())
			.compact();
	}

	public String extractUsername(String token) {
		return parseAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token) {
		Claims claims = parseAllClaims(token);
		return claims.getExpiration().after(new Date());
	}

	private Claims parseAllClaims(String token) {
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