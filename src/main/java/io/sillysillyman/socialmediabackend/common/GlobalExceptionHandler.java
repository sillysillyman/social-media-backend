package io.sillysillyman.socialmediabackend.common;

import io.sillysillyman.socialmediabackend.common.dto.ErrorMessage;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserException;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e
    ) {
        List<String> errorMessages = Stream.concat(
                e.getBindingResult().getFieldErrors().stream(),
                e.getBindingResult().getGlobalErrors().stream()
            )
            .map(ObjectError::getDefaultMessage)
            .toList();

        String combinedErrorMessage = String.join("\n", errorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorMessage(
                combinedErrorMessage,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name()
            )
        );
    }
}
