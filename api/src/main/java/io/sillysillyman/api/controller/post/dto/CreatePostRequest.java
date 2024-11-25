package io.sillysillyman.api.controller.post.dto;

import io.sillysillyman.core.domain.post.command.CreatePostCommand;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreatePostRequest(
    @NotNull(message = "content must not be null")
    @Length(max = 500, message = "content must be at most 500 characters")
    String content

    // TODO: 업로드 이미지 목록
    // List<MultipartFile> images;
) implements CreatePostCommand {

}
