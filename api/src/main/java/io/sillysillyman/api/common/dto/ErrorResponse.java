package io.sillysillyman.api.common.dto;

public record ErrorResponse(String detail, int status, String title) {

    public static ErrorResponse of(String detail, int status, String title) {
        return new ErrorResponse(detail, status, title);
    }
}
