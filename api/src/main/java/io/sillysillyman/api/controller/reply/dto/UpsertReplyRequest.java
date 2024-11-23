package io.sillysillyman.api.controller.reply.dto;

import io.sillysillyman.core.domain.reply.command.UpsertReplyCommand;
import jakarta.validation.constraints.NotBlank;

public record UpsertReplyRequest(
    @NotBlank(message = "content must be not blank")
    String content
) implements UpsertReplyCommand {

}
