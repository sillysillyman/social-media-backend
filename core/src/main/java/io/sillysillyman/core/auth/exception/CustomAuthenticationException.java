package io.sillysillyman.core.auth.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class CustomAuthenticationException extends AuthenticationException {

    private final AuthErrorCode authErrorCode;

    public CustomAuthenticationException(AuthErrorCode authErrorCode) {
        super(authErrorCode.getMessage());
        this.authErrorCode = authErrorCode;
    }

    public CustomAuthenticationException(AuthErrorCode authErrorCode, Throwable cause) {
        super(authErrorCode.getMessage(), cause);
        this.authErrorCode = authErrorCode;
    }
}
