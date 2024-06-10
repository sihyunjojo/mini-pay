package org.c4marathon.assignment.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.c4marathon.assignment.util.JwtConst.*;


@Slf4j
@Component
public class JwtProvider {
    private final Key key;

    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<Authentication> getAuthentication(String accessToken) {

        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITY_CLAIM) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클래임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(
                        claims.get(AUTHORITY_CLAIM)
                                .toString()
                                .split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        // UserDetails 객체를 만들어서 Authentication return
        // UserDetails : interface, User: UserDetails 를 구현한 class
        UserDetails principal = new User(claims.getSubject(), " ", authorities);
        return Optional.of(new UsernamePasswordAuthenticationToken(principal, " ", authorities));
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
//                    .setSigningKey(key)
                    .verifyWith((SecretKey) key)
                    .build()
//                    .parseClaimsJws(token);
//                    .parseEncryptedClaims(token)
                    .parseSignedClaims(token);

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    // accessToken
    //	전달 받은 토큰이 제대로 생성된 것인지 확인 하고 문제가 있다면 UnauthorizedException 발생.
    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
//                    .verifyWith(key)
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String resolveToken(HttpServletRequest request) {
        System.out.println(request.getRequestURI());
        String bearerToken = request.getHeader(HEADER_AUTH);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(GRANT_TYPE)) {
            // 앞에 Bearer(띄어쓰기) 제외하고 시작
            return bearerToken.substring(7);
        }
        return null;
    }
}
