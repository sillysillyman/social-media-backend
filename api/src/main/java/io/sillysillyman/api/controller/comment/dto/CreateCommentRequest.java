package io.sillysillyman.api.controller.comment.dto;

import io.sillysillyman.core.domain.comment.command.CreateCommentCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank(message = "content must not be blank")
    String content
) implements CreateCommentCommand {

}
