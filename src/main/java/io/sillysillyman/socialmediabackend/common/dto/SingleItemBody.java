package io.sillysillyman.socialmediabackend.common.dto;

public record SingleItemBody<T>(T data) {

    public static <T> SingleItemBody<T> from(T data) {
        return new SingleItemBody<>(data);
    }
}
