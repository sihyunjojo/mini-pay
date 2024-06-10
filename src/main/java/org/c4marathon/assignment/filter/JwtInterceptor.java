package org.c4marathon.assignment.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.c4marathon.assignment.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;

    public JwtInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = jwtProvider.resolveToken(request);
        if (token != null && jwtProvider.validateToken(token)) {
//            토큰이 유효할 경우 토큰 에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장

//            SecurityConfig에의해서 설정되어 있는 스프링 시큐리티에서 현재 실행 중인 스레드의 보안 컨텍스트를 가져옴

//            SecurityContextHolder는 현재 사용자의 보안 컨텍스트를 관리하는데 사용되는 스프링 시큐리티의 클래스입니다.
//            setAuthentication(authentication) 메서드는 전달된 authentication 객체를 보안 컨텍스트에 설정합니다.
//            이를 통해 스프링 시큐리티는 현재 사용자를 인증된 사용자로 설정하고,
//            이를 기반으로 요청을 처리합니다.

            // Add authentication to the context if token is valid
            jwtProvider.getAuthentication(token)
                    .ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication));
        }
        return true;  // Proceed with the request
    }
}