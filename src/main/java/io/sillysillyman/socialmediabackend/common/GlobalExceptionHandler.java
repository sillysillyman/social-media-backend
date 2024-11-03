package io.sillysillyman.socialmediabackend.common;

import io.sillysillyman.socialmediabackend.common.dto.ErrorResponse;
import io.sillysillyman.socialmediabackend.domain.comment.exception.CommentException;
import io.sillysillyman.socialmediabackend.domain.post.exception.PostException;
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

    @ExceptionHandler(CommentException.class)
    public ResponseEntity<ErrorResponse> handleCommentException(CommentException e) {
        log.error("error: ", e);
        return ResponseEntity.status(e.getCommentErrorCode().getStatus()).body(
            new ErrorResponse(
                e.getCommentErrorCode().getMessage(),
                e.getCommentErrorCode().getStatus().value(),
                e.getCommentErrorCode().getStatus().name()
            )
        );
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<ErrorResponse> handlePostException(PostException e) {
        log.error("error: ", e);
        return ResponseEntity.status(e.getPostErrorCode().getStatus()).body(
            new ErrorResponse(
                e.getPostErrorCode().getMessage(),
                e.getPostErrorCode().getStatus().value(),
                e.getPostErrorCode().getStatus().name()
            )
        );
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException e) {
        log.error("error: ", e);
        return ResponseEntity.status(e.getUserErrorCode().getStatus()).body(
            new ErrorResponse(
                e.getUserErrorCode().getMessage(),
                e.getUserErrorCode().getStatus().value(),
                e.getUserErrorCode().getStatus().name()
            )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
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
            new ErrorResponse(
                combinedErrorMessage,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name()
            )
        );
    }
}
