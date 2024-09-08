package org.c4marathon.assignment.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.c4marathon.assignment.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import static org.c4marathon.assignment.util.JwtConst.*;


@Slf4j
// 토큰의 유효성 검사를 책임지는 필터 클래스
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 1. Request Header 에서 JWT 토큰 추출
        String token = resolveToken((HttpServletRequest) request);
        System.out.println("token = " + token);
        // 2. validateToken 으로 토큰 유효성 검사
        // 이게 제공자 쪽 클래스에서 나오는게 맞나 -> 인증을 만들어주는 곳이니 검증도 같이 해주면 좋을듯.

        if (token != null && jwtProvider.validateToken(token)) {
            // 토큰이 유효할 경우 토큰 에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장
            Optional<Authentication> authentication = jwtProvider.getAuthentication(token);

//            SecurityConfig에의해서 설정되어 있는 스프링 시큐리티에서 현재 실행 중인 스레드의 보안 컨텍스트를 가져옴

//            SecurityContextHolder는 현재 사용자의 보안 컨텍스트를 관리하는데 사용되는 스프링 시큐리티의 클래스입니다.
//            setAuthentication(authentication) 메서드는 전달된 authentication 객체를 보안 컨텍스트에 설정합니다.
//            이를 통해 스프링 시큐리티는 현재 사용자를 인증된 사용자로 설정하고,
//            이를 기반으로 요청을 처리합니다.

            SecurityContextHolder.getContext().setAuthentication(authentication.orElse(null));
            log.info("토큰 사용 가능 : {}", token);

            // 이 뒤에 access token이
            chain.doFilter(request, response);
        } else {
            log.info("토큰 사용 불가능 : {}", token);
            chain.doFilter(request, response);
        }
    }

    // Request Header 에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        System.out.println(request.getRequestURI());
        String bearerToken = request.getHeader(HEADER_AUTH);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(GRANT_TYPE)) {
            // 앞에 Bearer(띄어쓰기) 제외하고 시작
            return bearerToken.substring(7);
        }
        return null;
    }
}
