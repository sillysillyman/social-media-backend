package io.sillysillyman.socialmediabackend.auth;

import io.sillysillyman.socialmediabackend.auth.constants.JwtConstants;
import io.sillysillyman.socialmediabackend.auth.dto.LoginDto;
import io.sillysillyman.socialmediabackend.auth.dto.TokenDto;
import io.sillysillyman.socialmediabackend.auth.service.AuthService;
import io.sillysillyman.socialmediabackend.common.dto.SingleItemBody;
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
    public ResponseEntity<SingleItemBody<TokenDto>> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(SingleItemBody.from(authService.login(loginDto)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<SingleItemBody<TokenDto>> refresh(
        @RequestHeader(JwtConstants.REFRESH_HEADER) String refreshToken
    ) {
        return ResponseEntity.ok(SingleItemBody.from(authService.refresh(refreshToken)));
    }
}
