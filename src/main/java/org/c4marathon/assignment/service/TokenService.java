package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.jwt.JwtGenerator;
import org.c4marathon.assignment.model.Token;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtGenerator jwtGenerator;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Value("${jwt.refresh-token.expiretime}")
    private long refreshTokenExpireTime;

    public void saveRefreshToken(User user, String refreshToken) throws Exception {
        Token token = new Token();
        token.setToken(refreshToken);
        token.setTokenType(Token.TokenType.REFRESH);
        token.setUser(user);
        token.setExpiryDate(new Date(System.currentTimeMillis() + refreshTokenExpireTime));

        tokenRepository.save(token);
    }

    public void deleteRefreshToken(Token refreshToken) {

        tokenRepository.delete(refreshToken);

    }
}
