package io.sillysillyman.socialmediabackend.domain.post.service;

import io.sillysillyman.socialmediabackend.auth.exception.AuthErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.socialmediabackend.domain.post.Post;
import io.sillysillyman.socialmediabackend.domain.post.dto.CreatePostRequest;
import io.sillysillyman.socialmediabackend.domain.post.dto.PostResponse;
import io.sillysillyman.socialmediabackend.domain.post.dto.UpdatePostRequest;
import io.sillysillyman.socialmediabackend.domain.post.exception.PostErrorCode;
import io.sillysillyman.socialmediabackend.domain.post.exception.detail.PostNotFoundException;
import io.sillysillyman.socialmediabackend.domain.post.repository.PostRepository;
import io.sillysillyman.socialmediabackend.domain.user.User;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Post getById(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(PostErrorCode.POST_NOT_FOUND));
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest createPostRequest, User user) {
        Post post = Post.builder().content(createPostRequest.getContent()).user(user).build();
        postRepository.save(post);
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return PostResponse.from(getById(postId));
    }

    @Transactional
    public void updatePost(Long postId, UpdatePostRequest updatePostRequest, User user) {
        Post post = getById(postId);
        validatePostOwnership(post.getUser().getId(), user.getId());
        post.update(updatePostRequest);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getById(postId);
        validatePostOwnership(post.getUser().getId(), user.getId());
        postRepository.delete(post);
    }

    private void validatePostOwnership(Long authorId, Long userId) {
        if (!Objects.equals(authorId, userId)) {
            throw new UnauthorizedAccessException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
