package io.sillysillyman.socialmediabackend.common;

import io.sillysillyman.socialmediabackend.common.dto.ErrorMessage;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorMessage> handleUserException(UserException e) {
        log.error("error: ", e);
        return ResponseEntity.status(e.getUserErrorCode().getStatus()).body(
            new ErrorMessage(
                e.getUserErrorCode().getMessage(),
                e.getUserErrorCode().getStatus().value(),
                e.getUserErrorCode().getStatus().name()
            )
        );
    }
}
