package io.sillysillyman.socialmediabackend.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateCommentRequest {

    @NotBlank(message = "content must not be blank")
    private String content;
}
