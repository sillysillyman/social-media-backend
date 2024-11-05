package io.sillysillyman.core.domain.post.exception.detail;

import io.sillysillyman.core.domain.post.exception.PostErrorCode;
import io.sillysillyman.core.domain.post.exception.PostException;

public class PostNotFoundException extends PostException {

    public PostNotFoundException(PostErrorCode postErrorCode) {
        super(postErrorCode);
    }
}
