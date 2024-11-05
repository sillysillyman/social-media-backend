package io.sillysillyman.core.domain.post.service;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.command.CreatePostCommand;
import io.sillysillyman.core.domain.post.command.UpdatePostCommand;
import io.sillysillyman.core.domain.post.exception.PostErrorCode;
import io.sillysillyman.core.domain.post.exception.detail.PostNotFoundException;
import io.sillysillyman.core.domain.post.repository.PostRepository;
import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.socialmediabackend.domain.post.dto.PostResponse;
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
    public PostResponse createPost(CreatePostCommand createPostCommand, User user) {
        Post post = Post.builder().content(createPostCommand.getContent()).user(user).build();
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
    public void updatePost(Long postId, UpdatePostCommand updatePostCommand, User user) {
        Post post = getById(postId);
        validatePostOwnership(user.getId(), post.getUser().getId());
        post.update(updatePostCommand);
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
