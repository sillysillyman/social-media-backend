package io.sillysillyman.socialmediabackend.domain.post.exception;

import lombok.Getter;

@Getter
public class PostException extends RuntimeException {

    private final PostErrorCode postErrorCode;

    public PostException(PostErrorCode postErrorCode) {
        super(postErrorCode.getMessage());
        this.postErrorCode = postErrorCode;
    }
}
