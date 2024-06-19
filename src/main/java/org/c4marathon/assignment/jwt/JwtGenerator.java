package org.c4marathon.assignment.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.c4marathon.assignment.model.User;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import static org.c4marathon.assignment.util.JwtConst.*;


@Component
public class JwtGenerator {
    private final Key key;

    @Value("${jwt.access-token.expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpireTime;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public JwtGenerator(@Value("${jwt.secret}") String secretKey, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

//    DaoAuthenticationProvider는 CustomUserDetails의 유효성을 검사한 다음 구성된 CustomUserDetailsService에서 반환된 CustomUserDetails인 주체가 있는 인증을 반환합니다.

    public JwtToken signIn(User user) {
        String nickname = user.getNickname();
        String password = user.getPassword();

        // 회원을 검증..
        // 목적 ?
        // 이 회원이 우리 사이트의 회원인가를 검증
        // 그럼 검증시, id와 name으로 검증하는게 좋을거 같은데
        // name은 unique하지 않으므로 nickname 생성

        // 1. nickname + password 를 기반으로 Authentication 객체 생성
        // 이때 authentication 은 인증 여부를 확인하는 authenticated 값이 false
        // 아직 인증을 하지 않고 토큰만 만들었으니

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(nickname, password);

        // token = UsernamePasswordAuthenticationToken [Principal=Mia Farrow, Credentials=[PROTECTED], Authenticated=false, Details=null, Granted Authorities=[]]
        // Credentials=[PROTECTED]  - 보안 상으로 비밀번호는 숨겨졌음을 의미
        // Authenticated=false - 토큰이 인증되었는지 여부
        // Details=null -  여기에는 IP 주소, 세션 ID 등 인증 요청에 대한 추가 세부정보가 포함될 수 있습니다. 기본적으로 setDetails(객체 세부정보) 메소드를 사용하여 명시적으로 설정하지 않는 한 null입니다.
        // GrantedAuthorities=[] - 설명: 주체에게 부여된 권한(역할 또는 권한)을 나타냅니다. 빈 목록 []은 할당된 권한이 없음을 나타냅니다. 이는 토큰이 처음 생성될 때와 인증 관리자 또는 공급자가 처리하기 전에 예상되는 현상입니다.
        System.out.println("token = " + authenticationToken);

        // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드 실행

        // Obtain the AuthenticationManager from the builder
        // Spring Security 설정에서 정의된 모든 인증 공급자(AuthenticationProvider)와 필터(Filter)를 기반으로 복잡한 구성을 통해 생성됩니다.
        AuthenticationManager authenticationManager = authenticationManagerBuilder.getObject();

        //Spring Security 애플리케이션에서 인증 관리를 담당하는 AuthenticationManager 인터페이스의 구현입니다.
        // ProviderManager는 인증 요청을 AuthenticationProvider 목록에 위임합니다.

        System.out.println(authenticationManager);
        // Authenticate the token
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        System.out.println("authentication = " + authentication);
        System.out.println();

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return generateToken(user, authentication);
    }

    public JwtToken generateToken(User user, Authentication authentication) {
        String accessToken = createAccessToken(user, authentication);
        String refreshToken = createRefreshToken(user.getId());

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public String createAccessToken(User user, Authentication authentication) {
        Long userId = user.getId();
        String nickname = user.getNickname();

        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        System.out.println(authorities);
        // 토큰 마저 만들기
        return createJwtBuilder(userId, "access-token", accessTokenExpireTime)
                .claim(AUTHORITY_CLAIM, authorities)
                .claim("nickname", nickname)
//                .claim("role", role)
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

        // 라이브 강사님 코드
//        String jwt = Jwts.builder()
////			Header 설정 : 토큰의 타입, 해쉬 알고리즘 정보 세팅.
//                .setHeaderParam("typ", "JWT").setClaims(claims)
////			Signature 설정 : secret key를 활용한 암호화.
//                .signWith(SignatureAlgorithm.HS256, this.generateKey())
//                .compact(); // 직렬화 처리.

        return Jwts.builder()
                .claims(claims)
//              이게 필요한 이유가 없다.
                // 괜한 보안에 위험할 수 도 있다.
                // 무작위 공격
                .claim("userId", userId)
                //비밀키의 크기에 따라 서명 알고리즘이 선택됩니다.
                .signWith(key);
    }


}
