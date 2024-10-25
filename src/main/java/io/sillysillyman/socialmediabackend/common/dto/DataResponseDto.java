package io.sillysillyman.socialmediabackend.common.dto;

public record DataResponseDto<T>(int status, T data, String message) {

}
