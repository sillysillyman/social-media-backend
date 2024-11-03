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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        // TODO: 팔로우/팔로잉 기반 공개/비공개 여부 검증 필요
        return postRepository.findByUserId(userId, pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(User user, Pageable pageable) {
        return postRepository.findByUserId(user.getId(), pageable).map(PostResponse::from);
    }

    @Transactional
    public void updatePost(Long postId, UpdatePostRequest updatePostRequest, User user) {
        Post post = getById(postId);
        validatePostOwnership(user.getId(), post.getUser().getId());
        post.update(updatePostRequest);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getById(postId);
        validatePostOwnership(user.getId(), post.getUser().getId());
        postRepository.delete(post);
    }

    private void validatePostOwnership(Long userId, Long authorId) {
        if (!Objects.equals(userId, authorId)) {
            throw new UnauthorizedAccessException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
