package io.sillysillyman.api.controller.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateCommentRequest {

    @NotBlank(message = "content must not be blank")
    private String content;
}
