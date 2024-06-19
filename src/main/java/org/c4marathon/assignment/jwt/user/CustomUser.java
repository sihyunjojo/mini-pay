package org.c4marathon.assignment.jwt.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;


// 핵심 사용자 정보를 모델링 한다.
// UserDetails의 구현을 작성할 수 있다.
// equals 및 hashcode 구현은 userNickname 속성만을 기반으로 합니다.
// 이 구현은 변경할 수 없습니다. 인증 후 비밀번호를 삭제할 수 있도록 CredentialsContainer 인터페이스를 구현합니다. 인스턴스를 메모리에 저장하고 재사용하는 경우 부작용이 발생할 수 있습니다. 그렇다면 호출될 때마다 UserDetailsService 에서 복사본을 반환해야 합니다.
// 작가: 벤 알렉스, 루크 테일러, 조시현

public class CustomUser implements CustomUserDetails, CredentialsContainer {


    @Serial // 뭐하는 어노테이션 이지??
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private static final Log logger = LogFactory.getLog(User.class);

    private String password;

    private final String userNickname;

    private final Set<GrantedAuthority> authorities;

    // 사용자 계정 만료 여부
    private final boolean accountNonExpired;

    // 사용자 계정 잠금 여부
    private final boolean accountNonLocked;

    // 자격증명 (비밀번호) 만료 여부
    private final boolean credentialsNonExpired;

    // 사용자 활성화 여부
    private final boolean enabled;

    public CustomUser(String userNickname, String password, Collection<? extends GrantedAuthority> authorities) {
        this(userNickname, password, true, true, true, true, authorities);
    }

    public CustomUser(String userNickname, String password, boolean enabled, boolean accountNonExpired,
                boolean credentialsNonExpired, boolean accountNonLocked,
                Collection<? extends GrantedAuthority> authorities) {
        Assert.isTrue(userNickname != null && !userNickname.isEmpty() && password != null,
                "Cannot pass null or empty values to constructor");
        this.userNickname = userNickname;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getUserNickname() {
        return this.userNickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    // 누군가에게 어떤 일을 맡길 때 관련 분야의 경험과 자격부터 살피게 되죠. 바로 그 경험과 자격을 credentials
    // 그것을 지움
    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new CustomUser.AuthorityComparator());
        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }
        return sortedAuthorities;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User user) {
            return this.userNickname.equals(user.getUsername());
        }
        return false;
    }

    /**
     * Returns the hashcode of the {@code userNickname}.
     */
    @Override
    public int hashCode() {
        return this.userNickname.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(" [");
        sb.append("userNickname=").append(this.userNickname).append(", ");
        // 패스워드는 보안이여서 그냥 이렇게 글자로 넣어줌.
        sb.append("Password=[PROTECTED], ");
        sb.append("Enabled=").append(this.enabled).append(", ");
        sb.append("AccountNonExpired=").append(this.accountNonExpired).append(", ");
        sb.append("credentialsNonExpired=").append(this.credentialsNonExpired).append(", ");
        sb.append("AccountNonLocked=").append(this.accountNonLocked).append(", ");
        sb.append("Granted Authorities=").append(this.authorities).append("]");
        return sb.toString();
    }


    /**
     * 지정된 사용자 이름으로 UserBuilder를 생성합니다.
     * @param userNickname – 사용할 사용자 이름
     * @return the UserBuilder
     */
    public static CustomUserBuilder withUserNickname(String userNickname) {
        return builder().userNickname(userNickname);
    }

    public static CustomUserBuilder builder() {
        return new CustomUser.CustomUserBuilder();
    }

    public static CustomUserBuilder withUserDetails(CustomUserDetails userDetails) {
        // @formatter:off
        return withUserNickname(userDetails.getUserNickname())
                .password(userDetails.getPassword())
                .accountExpired(!userDetails.isAccountNonExpired())
                .accountLocked(!userDetails.isAccountNonLocked())
                .authorities(userDetails.getAuthorities())
                .credentialsExpired(!userDetails.isCredentialsNonExpired())
                .disabled(!userDetails.isEnabled());
        // @formatter:on
    }


    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {

        @Serial
        private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

        @Override
        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            // Neither should ever be null as each entry is checked before adding it to
            // the set. If the authority is null, it is a custom authority and should
            // precede others.
            if (g2.getAuthority() == null) {
                return -1;
            }
            if (g1.getAuthority() == null) {
                return 1;
            }
            return g1.getAuthority().compareTo(g2.getAuthority());
        }

    }

    public static final class CustomUserBuilder {

        private String userNickname;

        private String password;

        private List<GrantedAuthority> authorities = new ArrayList<>();

        private boolean accountExpired;

        private boolean accountLocked;

        private boolean credentialsExpired;

        private boolean disabled;

        private Function<String, String> passwordEncoder = (password) -> password;

        /**
         * Creates a new instance
         */
        private CustomUserBuilder() {
        }

        /**
         * Populates the userNickname. This attribute is required.
         * @param userNickname the userNickname. Cannot be null.
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder userNickname(String userNickname) {
            Assert.notNull(userNickname, "userNickname cannot be null");
            this.userNickname = userNickname;
            return this;
        }

        /**
         * Populates the password. This attribute is required.
         * @param password the password. Cannot be null.
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder password(String password) {
            Assert.notNull(password, "password cannot be null");
            this.password = password;
            return this;
        }

        /**
         * Encodes the current password (if non-null) and any future passwords supplied to
         * {@link #password(String)}.
         * @param encoder the encoder to use
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder passwordEncoder(Function<String, String> encoder) {
            Assert.notNull(encoder, "encoder cannot be null");
            this.passwordEncoder = encoder;
            return this;
        }

        /**
         * Populates the roles. This method is a shortcut for calling
         * {@link #authorities(String...)}, but automatically prefixes each entry with
         * "ROLE_". This means the following:
         *
         * <code>
         *     builder.roles("USER","ADMIN");
         * </code>
         *
         * is equivalent to
         *
         * <code>
         *     builder.authorities("ROLE_USER","ROLE_ADMIN");
         * </code>
         *
         * <p>
         * This attribute is required, but can also be populated with
         * {@link #authorities(String...)}.
         * </p>
         * @param roles the roles for this user (i.e. USER, ADMIN, etc). Cannot be null,
         * contain null values or start with "ROLE_"
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder roles(String... roles) {
            List<GrantedAuthority> authorities = new ArrayList<>(roles.length);
            for (String role : roles) {
                Assert.isTrue(!role.startsWith("ROLE_"),
                        () -> role + " cannot start with ROLE_ (it is automatically added)");
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            return authorities(authorities);
        }

        /**
         * Populates the authorities. This attribute is required.
         * @param authorities the authorities for this user. Cannot be null, or contain
         * null values
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         * @see #roles(String...)
         */
        public CustomUserBuilder authorities(GrantedAuthority... authorities) {
            Assert.notNull(authorities, "authorities cannot be null");
            return authorities(Arrays.asList(authorities));
        }

        /**
         * Populates the authorities. This attribute is required.
         * @param authorities the authorities for this user. Cannot be null, or contain
         * null values
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         * @see #roles(String...)
         */
        public CustomUserBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            Assert.notNull(authorities, "authorities cannot be null");
            this.authorities = new ArrayList<>(authorities);
            return this;
        }

        /**
         * Populates the authorities. This attribute is required.
         * @param authorities the authorities for this user (i.e. ROLE_USER, ROLE_ADMIN,
         * etc). Cannot be null, or contain null values
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         * @see #roles(String...)
         */
        public CustomUserBuilder authorities(String... authorities) {
            Assert.notNull(authorities, "authorities cannot be null");
            return authorities(AuthorityUtils.createAuthorityList(authorities));
        }

        /**
         * Defines if the account is expired or not. Default is false.
         * @param accountExpired true if the account is expired, false otherwise
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder accountExpired(boolean accountExpired) {
            this.accountExpired = accountExpired;
            return this;
        }

        /**
         * Defines if the account is locked or not. Default is false.
         * @param accountLocked true if the account is locked, false otherwise
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder accountLocked(boolean accountLocked) {
            this.accountLocked = accountLocked;
            return this;
        }

        /**
         * Defines if the credentials are expired or not. Default is false.
         * @param credentialsExpired true if the credentials are expired, false otherwise
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder credentialsExpired(boolean credentialsExpired) {
            this.credentialsExpired = credentialsExpired;
            return this;
        }

        /**
         * Defines if the account is disabled or not. Default is false.
         * @param disabled true if the account is disabled, false otherwise
         * @return the {@link CustomUserBuilder} for method chaining (i.e. to populate
         * additional attributes for this user)
         */
        public CustomUserBuilder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public CustomUserDetails build() {
            String encodedPassword = this.passwordEncoder.apply(this.password);
            return new CustomUser(this.userNickname, encodedPassword, !this.disabled, !this.accountExpired,
                    !this.credentialsExpired, !this.accountLocked, this.authorities);
        }

    }

}
