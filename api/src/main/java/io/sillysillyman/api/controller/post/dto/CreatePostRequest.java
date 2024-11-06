package io.sillysillyman.api.controller.post.dto;

import io.sillysillyman.core.domain.post.command.CreatePostCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;


@Getter
public class CreatePostRequest implements CreatePostCommand {

    @NotBlank(message = "content must not be blank")
    @Length(max = 500, message = "content must be at most 500 characters")
    private String content;

    // TODO: 업로드 이미지 목록
    // List<MultipartFile> images;
}
