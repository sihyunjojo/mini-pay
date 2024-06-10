package org.c4marathon.assignment.util;

public class JwtConst {
    public final static String HEADER_AUTH = "Authorization";
    public final static String GRANT_TYPE = "Bearer";
    //    jwt.token.grant-type = "Bearer"는 OAuth 2.0 프로토콜에서 사용되는 용어입니다.
//    이 문구는 JWT 토큰의 사용 방법을 설명하고 있습니다.
//    여기서 "Bearer"는 OAuth 2.0의 인증 방식 중 하나로, 액세스 토큰을 전달하는 방식을 나타냅니다.
//    Bearer 토큰은 클라이언트가 액세스 권한을 얻었음을 증명하는데 사용됩니다.
    public final static String AUTHORITY_CLAIM = "auth";
    public final static String HEADER_ACCESS_TOKEN = "access-token";
    public final static String HEADER_REFRESH_TOKEN = "refresh-token";
}
