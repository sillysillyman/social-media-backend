package io.sillysillyman.api.controller.auth;

import io.sillysillyman.api.common.dto.SingleItemResponse;
import io.sillysillyman.api.controller.auth.dto.LoginRequest;
import io.sillysillyman.api.controller.auth.dto.TokenResponse;
import io.sillysillyman.core.auth.constants.JwtConstants;
import io.sillysillyman.core.auth.service.AuthService;
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
        return ResponseEntity.ok(
            SingleItemResponse.from(TokenResponse.from(authService.login(loginRequest)))
        );
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
        return ResponseEntity.ok(
            SingleItemResponse.from(TokenResponse.from(authService.refresh(refreshToken)))
        );
    }
}
