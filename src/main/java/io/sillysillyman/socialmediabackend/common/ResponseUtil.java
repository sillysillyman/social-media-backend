package io.sillysillyman.socialmediabackend.common;

import io.sillysillyman.socialmediabackend.common.dto.DataResponseDto;
import io.sillysillyman.socialmediabackend.common.dto.MessageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class ResponseUtil {

    public static ResponseEntity<MessageResponseDto> of(
        HttpStatus status,
        String message
    ) {
        return ResponseEntity.status(status)
            .body(new MessageResponseDto(status.value(), message));
    }

    public static <T> ResponseEntity<DataResponseDto<T>> of(
        HttpStatus status,
        T data,
        String message
    ) {
        return ResponseEntity.status(status)
            .body(new DataResponseDto<>(status.value(), data, message));
    }
}
