package io.sillysillyman.api.controller.comment.dto;

import io.sillysillyman.core.domain.comment.command.UpdateCommentCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateCommentRequest implements UpdateCommentCommand {

    @NotBlank(message = "content must not be blank")
    private String content;
}
