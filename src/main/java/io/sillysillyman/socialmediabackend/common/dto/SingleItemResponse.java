package io.sillysillyman.socialmediabackend.common.dto;

public record SingleItemResponse<T>(T data) {

    public static <T> SingleItemResponse<T> from(T data) {
        return new SingleItemResponse<>(data);
    }
}
