package io.sillysillyman.socialmediabackend.domain.post.exception.detail;

import io.sillysillyman.socialmediabackend.domain.post.exception.PostErrorCode;
import io.sillysillyman.socialmediabackend.domain.post.exception.PostException;

public class PostNotFoundException extends PostException {

    public PostNotFoundException(PostErrorCode postErrorCode) {
        super(postErrorCode);
    }
}
