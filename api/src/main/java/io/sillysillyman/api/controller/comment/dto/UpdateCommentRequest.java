package io.sillysillyman.api.controller.comment.dto;

import io.sillysillyman.core.domain.comment.command.UpdateCommentCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
    @NotBlank(message = "content must not be blank")
    String content
) implements UpdateCommentCommand {

}
