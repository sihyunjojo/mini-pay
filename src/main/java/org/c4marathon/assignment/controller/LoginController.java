package org.c4marathon.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.c4marathon.assignment.jwt.JwtGenerator;
import org.c4marathon.assignment.jwt.JwtToken;
import org.c4marathon.assignment.model.Token;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.service.LoginService;
import org.c4marathon.assignment.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.c4marathon.assignment.util.JwtConst.*;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final JwtGenerator jwtGenerator;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody User user) throws Exception {
        HttpStatus status;

        try {
            User loginMember = loginService.login(user.getEmail(), user.getPassword());
            log.info("loginMember = {} ", loginMember);

            if (loginMember != null) {
                System.out.println("success login");
                JwtToken jwToken = jwtGenerator.signIn(loginMember);

                log.info("jwToken = {} ", jwToken);
//				발급받은 refresh token 을 DB에 저장.
                tokenService.saveRefreshToken(loginMember, jwToken.refreshToken());

//				jwt 설정
//              "Bearer"는 HTTP 요청의 Authorization 헤더에 추가되는 값으로,
//              클라이언트가 서버에게 해당 요청이 보안 토큰(Bearer token)을 포함하고 있음을 알려줍니다.
//              "Bearer"는 표준화된 인증 체계인 OAuth 2.0에서 사용되며,
//              이를 통해 클라이언트가 서버에게 자신을 인증하고, 보호된 리소스에 접근할 수 있음을 알립니다.

                HttpHeaders headers = new HttpHeaders();
                headers.set(HEADER_AUTH, GRANT_TYPE + " " + jwToken);

                return ResponseEntity.ok().headers(headers).build();
            } else {
                status = HttpStatus.UNAUTHORIZED;
            }
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error(e.getMessage());
        }

        return new ResponseEntity<>(status);
    }


    @GetMapping("/logout")
    public ResponseEntity<?> removeToken(@RequestBody User user, Token refreshToken) {
        {
            Map<String, Object> resultMap = new HashMap<>();
            HttpStatus status;
            try {
                // 리프레시 토큰을 전부 식제해야한다고 생각.
                // 하나만

                // 유저랑 토큰 맞는지 확인
                // 다르면 수상한 ip나 아이디로 체크
                tokenService.deleteRefreshToken(refreshToken);
                status = HttpStatus.OK;
            } catch (Exception e) {
                log.error("로그아웃 실패 : {}", e);
                resultMap.put("message", e.getMessage());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return new ResponseEntity<>(resultMap, status);
        }
    }

    // acesstoken은 항상 같이 들어오는데
    // acesstoken에서 권한이랑 유니크값을 가지고 멤버의 정보를 알 수 있음.

    // 리프레시토큰은 없으면 없다고 클라이언트에게 보내줌. 401 UNAUTHORIZED
    // 리프레시토큰이 없어져가면, 클라이언트가 팝업으로 없어져 간다고 띄워주고 업데이트 해달라고하면
    // 서버에게 리프래시 토큰 재 발급을 요청.

    // 클라이언트에서 refresh token이 거의 다 되가면
    // refresh token이 유효한지 확인을 하고
    // 토큰이 유효하다면 accesstoken을 보내주고
    // 두 부분을 따로 적용해야하나?
    // 토큰이 유효하지 않다면 refresh token을 갱신하고 accesstoken도 보내줌.

// 나중에 자동 로그인도 적용.

    // 카카오 oauth 리프레시 토큰 정책?
    // access_token은 발급 받은 후 12시간-24시간(정책에 따라 변동 가능)동안 유효합니다. refresh token은 한달간 유효하며,
    // refresh token 만료가 1주일 이내로 남은 시점에서 사용자 토큰 갱신 요청을 하면 갱신된 access token과 갱신된 refresh token이 함께 반환됩니다.
    // 음.. 그런데 탈취를 생각하면 갱신(=만료시간 늘려줌)보다는 새로 만드는게 좋지 않나??
    // 음.. 그러면 새로 만들고 기존의 것은 지워주는게 좋을까? 아니면 만료일이 끝날때까지 두는게 좋을까?
    // access token 중에 가장 긴 것을 골라서 찾아야한다.


    // 보안 정책
    // BackEnd의 입장만 생각하면 단순히 JWT Access 토큰을 발행하고 인증하여 유저를 구분하면 된다고 생각할 수 있다.
    // 하지만 토큰이 탈취된다고 생각해보면 공격자가 사용자의 정보가 자유롭게 핸들링하는 정말 아찔한 상황이 연출된다.


    // NoSQL의 TTL을 이용하여 Refresh 토큰을 일정 시간만큼만 저장한다.
    // 이거 어떻게 해?? 신기하네

    // 1번째 방법은 토큰 자체에서 만료시간을 관리하니 별도로 NoSQL을 사용하지 않아도 된다고 판단되어 생각한 방식이다.
    // 하지만 Access, Refresh 모두 탈취당하면? 이 때는 2번째 방법을 이용한다면 NoSQL을 이용해 보다 싼 비용으로 Refresh 토큰을 강제로 만료시킬 수 있다.

//    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshToken(@RequestBody MemberDto memberDto, HttpServletRequest request)
//            throws Exception {
//        Map<String, Object> resultMap = new HashMap<>();
//        HttpStatus status = HttpStatus.ACCEPTED;

    // 헤더에서 가져와서
//        String token = request.getHeader("refreshToken");
//        log.debug("token : {}, memberDto : {}", token, memberDto);
    // //	전달 받은 토큰이 제대로 생성된 것인지 확인 하고 문제가 있다면 UnauthorizedException 발생.
//        if (jwtUtil.checkToken(token)) {
    // 같은게 있는지 확인하고 같은게 있으면 보내줌.
//            if (token.equals(memberService.getRefreshToken(memberDto.getMemberId()))) {
//                String accessToken = jwtUtil.createAccessToken(memberDto.getMemberId());
//                log.debug("token : {}", accessToken);
//                log.debug("정상적으로 access token 재발급!!!");
//                resultMap.put("access-token", accessToken);
//                status = HttpStatus.CREATED;
//            }
//        } else {
//            log.debug("refresh token 도 사용 불가!!!!!!!");
//            status = HttpStatus.UNAUTHORIZED;
//        }
//        return new ResponseEntity<Map<String, Object>>(resultMap, status);
//    }

//    @GetMapping("/refresh")
//    public ResponseEntity<Void> refresh(HttpServletRequest request, @Login AuthInfo authInfo) {
    // 요청 받은 헤더에서 원하는 헤더가 있는지 확인?
//        validateExistHeader(request);

    // 유저 인포인가봄
//        Long memberId = authInfo.getId();
    //
//        String refreshToken = AuthorizationExtractor.extractRefreshToken(request);
//
//        refreshTokenService.matches(refreshToken, memberId);
//
//        String accessToken = tokenManager.createAccessToken(authInfo);
//
//        return ResponseEntity.noContent()
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
//                .build();
//    }

//    @GetMapping("/refresh")
//    public ResponseEntity<TokenDto> refresh(@RequestBody TokenDto token) throws Exception {
//        return new ResponseEntity<>( memberService.refreshAccessToken(token), HttpStatus.OK);
//    }

//    private void validateExistHeader(HttpServletRequest request) {
//        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        String refreshTokenHeader = request.getHeader("Refresh-Token");
//        if (Objects.isNull(authorizationHeader) || Objects.isNull(refreshTokenHeader)) {
//            throw new TokenNotFoundException();
//        }
//    }
}
