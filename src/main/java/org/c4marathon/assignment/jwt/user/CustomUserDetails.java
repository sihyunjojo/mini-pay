package org.c4marathon.assignment.jwt.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

public interface CustomUserDetails extends Serializable, UserDetails {

    /**
     * 사용자에게 부여된 권한을 반환합니다. null 반환할 수 없습니다.
     * @return 자연 키로 정렬된 권한( null 이 아님)
     */
    Collection<? extends GrantedAuthority> getAuthorities();

    String getPassword();
    String getUserNickname();

    /**
     * 사용자의 계정이 만료되었는지 여부를 나타냅니다. 만료된 계정은 인증할 수 없습니다.
     * @return 사용자 계정이 유효한 경우(만료되지 않은 경우) true , 더 이상 유효하지 않은 경우(만료된 경우) false
     */
    boolean isAccountNonExpired();

    /**
     * 사용자가 잠겨 있는지 또는 잠금 해제되어 있는지를 나타냅니다. 잠긴 사용자는 인증할 수 없습니다.
     * @return 사용자가 잠겨 있지 않으면 true , 그렇지 않으면 false
     */
    boolean isAccountNonLocked();

    /**
     * 사용자의 자격 증명(비밀번호)이 만료되었는지 여부를 나타냅니다. 만료된 자격 증명으로 인해 인증이 불가능합니다.
     * @return 사용자의 자격 증명이 유효한 경우(즉, 만료되지 않은 경우) true, 더 이상 유효하지 않은 경우(즉, 만료된 경우) false
     */
    boolean isCredentialsNonExpired();

    /**
     * 사용자가 활성화되었는지 또는 비활성화되었는지 여부를 나타냅니다. 비활성화된 사용자는 인증될 수 없습니다.
     * @return 사용자가 활성화되어 있으면 true , 그렇지 않으면 false
     */
    boolean isEnabled();

}
