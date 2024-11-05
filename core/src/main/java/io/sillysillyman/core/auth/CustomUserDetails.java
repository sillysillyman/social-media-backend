package io.sillysillyman.core.auth;

import io.sillysillyman.core.domain.user.User;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserDetails(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO: 계정 만료 검증 (e.g. 회원가입 후 이메일 인증)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO: 계정 잠금 상태 (e.g. 관리자 수동 잠금, 로그인 실패 횟수 초과)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO: 비밀번호 정책 검증 (e.g. 6개월마다 변경)
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getDeletedAt() == null;
    }
}
