package org.c4marathon.assignment.jwt;

import lombok.Builder;

@Builder
public record JwtToken(
        String grantType,
        String accessToken,
        String refreshToken){
}

