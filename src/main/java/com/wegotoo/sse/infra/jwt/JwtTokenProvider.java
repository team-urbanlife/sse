package com.wegotoo.sse.infra.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtTokenValidator tokenValidator;

    public JwtTokenProvider(
            SecretKeyProvider secretKeyProvider,
            JwtTokenValidator tokenValidator
    ) {
        this.secretKey = secretKeyProvider.getSecretKey();
        this.tokenValidator = tokenValidator;
    }

    public boolean isValid(String token) {
        return tokenValidator.isValid(token);
    }

    public Optional<Long> extractSubId(String token) {
        if (!tokenValidator.isValidToken(token)) {
            return Optional.empty();
        }

        String sub = extractClaims(token).getSubject();
        return Optional.of(Long.valueOf(sub));
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
