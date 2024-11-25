package io.sillysillyman.api.controller.post.dto;

import io.sillysillyman.core.domain.post.command.UpdatePostCommand;
import jakarta.validation.constraints.NotNull;

public record UpdatePostRequest(
    @NotNull(message = "content must not be null")
    String content

    // TODO: 삭제할 이미지 ID 목록
    // private List<Long> deleteImageIds;

    // TODO: 추가할 새 이미지 목록
    // private List<MultipartFile> newImages;
) implements UpdatePostCommand {

}
