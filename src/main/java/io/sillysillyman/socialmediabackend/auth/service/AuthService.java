package io.sillysillyman.socialmediabackend.auth.service;

import io.sillysillyman.socialmediabackend.auth.CustomUserDetails;
import io.sillysillyman.socialmediabackend.auth.JwtUtil;
import io.sillysillyman.socialmediabackend.auth.dto.LoginDto;
import io.sillysillyman.socialmediabackend.auth.dto.TokenDto;
import io.sillysillyman.socialmediabackend.auth.exception.detail.AuthenticationFailedException;
import io.sillysillyman.socialmediabackend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public TokenDto login(LoginDto loginDto) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    loginDto.getUsername(),
                    loginDto.getPassword()
                )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.user();

            String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole());

            // TODO: Redis 활용하여 refresh 토큰 저장 로직

            return TokenDto.of(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Invalid username or password", e);
        }
    }

    @Transactional
    public TokenDto refresh(String refreshToken) {
        // TODO: 토큰 제발급 로직
        return null;
    }
}
