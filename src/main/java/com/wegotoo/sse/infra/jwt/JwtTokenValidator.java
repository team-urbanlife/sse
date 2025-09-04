package com.wegotoo.sse.infra.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenValidator {

    private final SecretKey secretKey;

    public JwtTokenValidator(SecretKeyProvider secretKeyProvider) {
        this.secretKey = secretKeyProvider.getSecretKey();
    }

    public boolean isValid(String token) {
        return isBearerToken(token) && isValidToken(removeBearer(token));
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException e) {
            return false;
        }
    }

    private boolean isBearerToken(String token) {
        return token.startsWith("Bearer ");
    }

    private String removeBearer(String token) {
        return token.replace("Bearer ", "");
    }

}
