package org.c4marathon.assignment.config;

import org.c4marathon.assignment.filter.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .order(1)
                .addPathPatterns("/**"); // Apply interceptor to all paths
//                .excludePathPatterns("/member/join", "/member/login", "/board/list", "/board/**"); // Exclude some paths
    }
}