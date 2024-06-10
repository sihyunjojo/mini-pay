package org.c4marathon.assignment.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;

@Component
public class JwtInterpreter {

    private final Key key;

    public JwtInterpreter(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getUserId(String authorization){
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(authorization.substring(7))
                .getPayload();

        System.out.println("claims = " + claims);
        return claims.get("userId", Long.class);
    }

    // 강사님 코드.
//    public String getUserId(String authorization) {
//        Jws<Claims> claims = null;
//        try {
//            claims = Jwts.parser().setSigningKey(this.generateKey())
//                    .parseClaimsJws(authorization);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new UnAuthorizedException();
//        }
//        Map<String, Object> value = claims.getBody();
//        log.info("value : {}", value);
//        return (String) value.get("userId");
//    }

}

