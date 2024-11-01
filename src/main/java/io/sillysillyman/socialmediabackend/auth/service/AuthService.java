package io.sillysillyman.socialmediabackend.auth.service;

import io.sillysillyman.socialmediabackend.auth.CustomUserDetails;
import io.sillysillyman.socialmediabackend.auth.dto.LoginRequest;
import io.sillysillyman.socialmediabackend.auth.dto.TokenResponse;
import io.sillysillyman.socialmediabackend.auth.exception.AuthErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.detail.AuthenticationFailedException;
import io.sillysillyman.socialmediabackend.auth.exception.detail.InvalidTokenException;
import io.sillysillyman.socialmediabackend.auth.exception.detail.TokenNotFoundException;
import io.sillysillyman.socialmediabackend.auth.repository.RefreshTokenRepository;
import io.sillysillyman.socialmediabackend.auth.util.JwtUtil;
import java.util.Collection;
import java.util.Objects;
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

@RequiredArgsConstructor
@Slf4j(topic = "AuthService")
@Service
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public TokenResponse login(LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            String accessToken = jwtUtil.generateAccessToken(username, authorities);
            String refreshToken = jwtUtil.generateRefreshToken(username, authorities);

            refreshTokenRepository.save(username, refreshToken);

            return TokenResponse.of(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException(AuthErrorCode.AUTHENTICATION_FAILED, e);
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

    public TokenResponse refresh(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new InvalidTokenException(AuthErrorCode.INVALID_TOKEN);
        }

        String username = jwtUtil.extractUsername(refreshToken);
        String savedRefreshToken = refreshTokenRepository.findByUsername(username).orElseThrow(() ->
            new TokenNotFoundException(TokenStorageErrorCode.REFRESH_TOKEN_NOT_FOUND)
        );

        if (!Objects.equals(refreshToken, savedRefreshToken)) {
            throw new AuthenticationFailedException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        Collection<? extends GrantedAuthority> authorities =
            jwtUtil.extractAuthorities(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(username, authorities);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, authorities);

        refreshTokenRepository.deleteByUsername(username);
        refreshTokenRepository.save(username, newRefreshToken);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}
