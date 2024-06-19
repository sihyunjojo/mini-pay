package org.c4marathon.assignment.config;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.filter.JwtAuthenticationFilter;
import org.c4marathon.assignment.jwt.JwtProvider;
import org.c4marathon.assignment.jwt.user.CustomUserDetailsService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{
    private final JwtProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;


//    이 인스턴스는 Spring에서 Bean으로 관리되며 들어오는 HTTP 요청을 처리하는 데 사용됩니다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
                .cors(AbstractHttpConfigurer::disable) // cors 비활성화 -> 프론트와 연결 시 따로 설정 필요
                // 아래 3 코드가 기본 로그인화면 띄워주는 창을 생성하는것을 막아주는거 같음
                // 문서에서는 Spring Security provides the following built-in mechanisms for reading a username and password from HttpServletRequest: 라고 표현
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 login form 비활성화
                .logout(AbstractHttpConfigurer::disable) // 기본 logout 비활성화

                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // jwt를 사용하기 때문에 세션 사용하지 않음

                .authorizeHttpRequests(authorize ->
                                authorize
                // 정적 리소스는 허락하는 코드
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                // 이 밖에 모든 요청에 대해서 인증을 필요로 한다는 설정
//                )
                .requestMatchers("/**").permitAll()
                // USER 권한이 있어야 요청할 수 있음
//                .requestMatchers("/members/test").hasRole("USER")
                .anyRequest().authenticated()
                )
//                .and()

                // JWT 인증을 위하여 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class).build();

//              HttpSecurity#addFilterAfter to add the filter after a particular filter
//              or HttpSecurity#addFilterAt to add the filter at a particular filter position in the filter chain.
                // 아래의 클래스들은 스프링 내장 컨테이너가 자동으로 빈으로 넣으니
                // @Component또는 빈으로 등록하면 중복으로 인해서 안된다.
                // 아래 코드를 통해서 filter를 지정해주고 작동시킴.

                // UsernamePasswordAuthenticationFilter 내부에서
                // if the request is a login request (e.g., /login with POST method), the attemptAuthentication method is executed.
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용
        // DelegatingPasswordEncoder: 이 팩토리 메소드는 DelegatingPasswordEncoder를 생성합니다.
        // 'DelegatingPasswordEncoder'는 여러 인코딩 체계를 지원할 수 있으며 인코딩된 비밀번호와 함께 저장된 접두사를 기반으로 적절한 'PasswordEncoder'에 위임합니다.
        PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // org.springframework.security.crypto.password.DelegatingPasswordEncoder@7a791e8b
        System.out.println("passwordEncoder = " + delegatingPasswordEncoder);
        return delegatingPasswordEncoder;
    }


    //Spring Security 구성에 정의된 AuthenticationManager 빈은 인증 프로세스를 처리하는 데 필수적입니다.
//    ProviderManager는 가장 일반적으로 사용되는 AuthenticationManager 구현입니다.
//    ProviderManager는 AuthenticationProvider 인스턴스 목록에 위임합니다.
//    각 AuthenticationProvider에는 인증이 성공해야 하는지, 실패해야 하는지 표시하거나 결정을 내릴 수 없음을 표시하고 다운스트림 AuthenticationProvider가 결정하도록 허용할 수 있는 기회가 있습니다.
//    구성된 AuthenticationProvider 인스턴스 중 어느 것도 인증할 수 없는 경우 ProviderNotFoundException과 함께 인증이 실패합니다. 이는 ProviderManager가 전달된 인증 유형을 지원하도록 구성되지 않았음을 나타내는 특수 AuthenticationException입니다.


    //public <C> C getSharedObject(Class<C> sharedType) {
    //        return this.sharedObjects.get(sharedType);
    //    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        // 하나 이상의 AuthenticationProvider에 인증을 위임합니다.

        // HttpSecurity 컨텍스트에서 AuthenticationManagerBuilder를 검색합니다.
        // 이 빌더는 AuthenticationManager를 구성하는 데 사용됩니다.
        // 이 인스턴스는 AuthenticationManagerBuilder에 제공된 설정을 기반으로 빌드됩니다.

//        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
        // getSharedObject()를 하면 AbstractConfiguredSecurityBuilder에서 sharedObjects(Map)에 있는  AuthenticationManagerBuilder에 해당하는 값이 나옴

        // 기본으로 AuthenticationManagerBuilder 중에 DefaultPasswordEncoderAuthenticationManagerBuilder를 넣어서
        //  ProviderManager에 넣어주는거 같다.

        AuthenticationManagerBuilder sharedObject = http.getSharedObject(AuthenticationManagerBuilder.class);
        // HttpSecurityConfiguration$DefaultPasswordEncoderAuthenticationManagerBuilder@755c29b0
        System.out.println("builder =" + sharedObject);
        System.out.println(sharedObject.getClass());

        //AuthenticationManagerBuilder를 사용하여 CustomUserDetailsService와 PasswordEncoder를 설정합니다.
        sharedObject.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());


        AuthenticationManager build = sharedObject.build();
        // org.springframework.security.authentication.ProviderManager
        System.out.println(build);
        return  build;
    }


    // 에러 방지 빈 2개
    @Bean
    public HttpFirewall allowUrlEncodedDoubleSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(allowUrlEncodedDoubleSlashHttpFirewall())
                .ignoring()
                // 아래를 무시해라
                // error endpoint를 열어줘야 함, favicon.ico 추가!
                .requestMatchers("/error", "/favicon.ico")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}

