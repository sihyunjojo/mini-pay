package org.c4marathon.assignment.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import static org.c4marathon.assignment.util.JwtConst.*;


@Component
public class JwtGenerator {
    private final Key key;

    @Value("${jwt.access-token.expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpireTime;

    public JwtGenerator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 토큰 마저 만들기
        return createJwtBuilder(userId, "access-token", accessTokenExpireTime)
                .claim(AUTHORITY_CLAIM,authorities)
                .compact();
    }

    //	AccessToken에 비해 유효기간을 길게 설정.
    public String createRefreshToken(Long userId) {
        // 토큰 마저 만들기
        return createJwtBuilder(userId, "refresh-token", refreshTokenExpireTime)
                .compact();
    }

//		key : Claim에 셋팅될 key 값
//		value : Claim에 셋팅 될 data 값
//		subject : payload에 sub의 value로 들어갈 subject값
//		expire : 토큰 유효기간 설정을 위한 값

    private JwtBuilder createJwtBuilder(Long userId, String subject, long expireTime) {
//        long now = (new Date()).getTime();
//        Date accessTokenExpiresIn = new Date(now + accessTokenExpireTime);

//		Payload 설정 : 생성일 (IssuedAt), 유효기간 (Expiration),
//		토큰 제목 (Subject), 데이터 (Claim) 등 정보 세팅.
        Claims claims = Jwts.claims()
                .subject(subject) // 토큰 제목 설정 ex) access-token, refresh-token
                .issuedAt(new Date()) // 생성일 설정
//				만료일 설정 (유효기간)
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .build();

        // 라이브 강사님 코드
//		저장할 data의 key, value
//        claims.put("userId", userId);

        JwtBuilder jwt = Jwts.builder()
                .claims(claims)
                // 강사님 코드 이렇게 하면 될거 같음.
                .claim("userId", userId)
                //비밀키의 크기에 따라 서명 알고리즘이 선택됩니다.
                .signWith(key);

        // 라이브 강사님 코드
//        String jwt = Jwts.builder()
////			Header 설정 : 토큰의 타입, 해쉬 알고리즘 정보 세팅.
//                .setHeaderParam("typ", "JWT").setClaims(claims)
////			Signature 설정 : secret key를 활용한 암호화.
//                .signWith(SignatureAlgorithm.HS256, this.generateKey())
//                .compact(); // 직렬화 처리.

        return jwt;
    }


    public JwtToken generateToken(Long userId, Authentication authentication) {
        String accessToken = createAccessToken(userId, authentication);
        String refreshToken = createRefreshToken(userId);

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


}
