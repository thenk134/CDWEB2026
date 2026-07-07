package com.news2026.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;

public class JwtUtil {
    // Secret key for HMAC256 signing (at least 256 bits/32 bytes)
    private static final String SECRET = "news2026-very-secure-secret-key-for-jwt-signing-123456789";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final String ISSUER = "news2026";
    // Token validity period: 24 hours (in milliseconds)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    public static String generateToken(String username, String role) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("username", username)
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(ALGORITHM);
    }

    public static String verifyTokenAndGetUsername(String token) {
        try {
            return JWT.require(ALGORITHM)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getClaim("username")
                    .asString();
        } catch (Exception e) {
            return null; // Invalid or expired token
        }
    }
}
