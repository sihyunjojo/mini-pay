package org.c4marathon.assignment.service;


import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    //    사용자 이름과 비밀번호를 읽기 위해 지원되는 각 메커니즘은 지원되는 모든 저장 메커니즘을 사용할 수 있습니다.

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//  이 부분이 자동으로 실행
    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        // name이 중복이 없다고 가정.
        System.out.println("nickname = " + nickname);
        Optional<org.c4marathon.assignment.model.User> byName = userRepository.findByNickname(nickname);

        if (byName.isEmpty()) {
            throw new UsernameNotFoundException("User not found with nickname: " + nickname);
        }

        UserDetails userDetails = byName
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다."));
        System.out.println("result = " + userDetails);

        return userDetails;
    }

    // 해당하는 User 의 데이터가 존재한다면 UserDetails 객체로 만들어서 return
    private UserDetails createUserDetails(org.c4marathon.assignment.model.User member) {
        UserDetails build = User.builder()
                .username(member.getName())
                .password(passwordEncoder.encode(member.getPassword()))
                .roles(member.getRole())
                .build();
        System.out.println("createUserDetails = " + build);
        return build;
    }

}