package io.sillysillyman.socialmediabackend.auth.service;

import io.sillysillyman.socialmediabackend.auth.CustomUserDetails;
import io.sillysillyman.socialmediabackend.auth.JwtUtil;
import io.sillysillyman.socialmediabackend.auth.dto.LoginDto;
import io.sillysillyman.socialmediabackend.auth.dto.TokenDto;
import io.sillysillyman.socialmediabackend.auth.exception.AuthErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.detail.AuthenticationFailedException;
import io.sillysillyman.socialmediabackend.auth.repository.RefreshTokenRepository;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j(topic = "AuthService")
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    public TokenDto login(LoginDto loginDto) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    loginDto.getUsername(),
                    loginDto.getPassword()
                )
            );

            UserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            String accessToken = jwtUtil.generateAccessToken(username, authorities);
            String refreshToken = jwtUtil.generateRefreshToken(username, authorities);

            refreshTokenRepository.save(username, refreshToken);

            return TokenDto.of(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException(
                AuthErrorCode.AUTHENTICATION_FAILED.getMessage(), e);
        }
    }

    public void logout() {
        UserDetails userDetails =
            (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String username = userDetails.getUsername();
        refreshTokenRepository.deleteByUsername(username);
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully: {}", username);
    }

    @Transactional
    public TokenDto refresh(String refreshToken) {
        // TODO: 토큰 재발급 로직
        return null;
    }
}
