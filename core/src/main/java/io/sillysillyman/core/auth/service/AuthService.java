package io.sillysillyman.core.auth.service;

import io.sillysillyman.core.auth.CustomUserDetails;
import io.sillysillyman.core.auth.Token;
import io.sillysillyman.core.auth.command.LoginCommand;
import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.detail.AuthenticationFailedException;
import io.sillysillyman.core.auth.exception.detail.InvalidTokenException;
import io.sillysillyman.core.auth.exception.detail.TokenNotFoundException;
import io.sillysillyman.core.auth.repository.RefreshTokenRepository;
import io.sillysillyman.core.auth.util.JwtUtil;
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

    public Token login(LoginCommand loginCommand) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    loginCommand.getUsername(),
                    loginCommand.getPassword()
                )
            );

            UserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            String accessToken = jwtUtil.generateAccessToken(username, authorities);
            String refreshToken = jwtUtil.generateRefreshToken(username, authorities);

            refreshTokenRepository.save(username, refreshToken);

            return Token.of(accessToken, refreshToken);
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

    public Token refresh(String refreshToken) {
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

        return Token.of(newAccessToken, newRefreshToken);
    }
}
