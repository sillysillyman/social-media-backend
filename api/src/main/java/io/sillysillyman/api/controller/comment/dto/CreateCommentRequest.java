package io.sillysillyman.api.controller.comment.dto;

import io.sillysillyman.core.domain.comment.command.CreateCommentCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateCommentRequest implements CreateCommentCommand {

    @NotBlank(message = "content must not be blank")
    private String content;
}
