package io.sillysillyman.socialmediabackend.common.dto;

public record ErrorMessage(String detail, int status, String title) {

    public static ErrorMessage of(String detail, int status, String title) {
        return new ErrorMessage(detail, status, title);
    }
}
