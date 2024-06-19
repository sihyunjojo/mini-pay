package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/*
    클래스 이름을 'Token'으로 지정하면 특정 구현 세부정보를 추상화할 수 있습니다.
    이를 통해 향후 더 큰 유연성을 확보할 수 있습니다.
    토큰 구현을 변경하기로 결정한 경우(예: JWT에서 다른 유형의 토큰으로 전환 또는 추가 토큰 유형에 대한 지원 추가)
    클래스 이름을 바꾸거나 코드베이스 전체에서 해당 참조를 업데이트할 필요가 없습니다.

    이름에 'Jwt'를 포함하지 않으면 클래스를 특정 구현에 묶지 않음으로써 SRP(단일 책임 원칙)를 더 밀접하게 준수하게 됩니다.

    상황에 따른 명확성
    애플리케이션 내에서 사용될 때 컨텍스트를 통해 토큰이 JWT임을 분명히 알 수 있는 경우가 많습니다.
     특히 JWT 생성 및 처리를 위한 전용 서비스가 있는 경우 더욱 그렇습니다. 이렇게 하면 중복을 방지하고 클래스 이름을 간결하게 유지할 수 있습니다.

     확장성
     OAuth 토큰, API 키 또는 사용자 정의 세션 토큰이 있을 수 있습니다
 */
@Data
@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    // 추후 redis 이용시 적용 가능?
    // Package org.springframework.data.redis.core
    // 만료 기간을 설정해주는 어노테이션
//    @TimeToLive(unit = TimeUnit.SECONDS)
    @Column(nullable = false)
    private Date expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    public enum TokenType {
        REFRESH, OTHER // Flexibility to add more types
//        ACCESS,
    }
}
