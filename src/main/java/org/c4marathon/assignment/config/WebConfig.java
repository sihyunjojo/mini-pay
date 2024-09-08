package org.c4marathon.assignment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost","http://localhost:8080") // Use allowedOriginPatterns
                .allowedMethods("*") // Allow all methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow credentials
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/assets/img/");
        registry.addResourceHandler("/*.html**").addResourceLocations("classpath:/static/");
    }
}