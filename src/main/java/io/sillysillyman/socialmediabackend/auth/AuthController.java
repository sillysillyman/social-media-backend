package io.sillysillyman.socialmediabackend.auth;

import io.sillysillyman.socialmediabackend.auth.constants.JwtConstants;
import io.sillysillyman.socialmediabackend.auth.dto.LoginRequest;
import io.sillysillyman.socialmediabackend.auth.dto.TokenResponse;
import io.sillysillyman.socialmediabackend.auth.service.AuthService;
import io.sillysillyman.socialmediabackend.common.dto.SingleItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<SingleItemResponse<TokenResponse>> login(
        @Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(SingleItemResponse.from(authService.login(loginRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<SingleItemResponse<TokenResponse>> refresh(
        @RequestHeader(JwtConstants.REFRESH_HEADER) String refreshToken
    ) {
        return ResponseEntity.ok(SingleItemResponse.from(authService.refresh(refreshToken)));
    }
}
