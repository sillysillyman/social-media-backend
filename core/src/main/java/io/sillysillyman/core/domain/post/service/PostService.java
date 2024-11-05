package io.sillysillyman.core.domain.post.service;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.post.command.CreatePostCommand;
import io.sillysillyman.core.domain.post.command.UpdatePostCommand;
import io.sillysillyman.core.domain.post.exception.PostErrorCode;
import io.sillysillyman.core.domain.post.exception.detail.PostNotFoundException;
import io.sillysillyman.core.domain.post.repository.PostRepository;
import io.sillysillyman.core.domain.user.User;
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
        return Post.from(
            postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(PostErrorCode.POST_NOT_FOUND)
            )
        );
    }

    @Transactional
    public Post createPost(CreatePostCommand createPostCommand, User user) {
        Post post = Post.builder()
            .user(user)
            .content(createPostCommand.getContent())
            .build();

        PostEntity postEntity = postRepository.save(PostEntity.from(post));

        return Post.from(postEntity);
    }

    @Transactional(readOnly = true)
    public Post getPost(Long postId) {
        return getById(postId);
    }

    @Transactional(readOnly = true)
    public Page<Post> getUserPosts(Long userId, Pageable pageable) {
        // TODO: 팔로우/팔로잉 기반 공개/비공개 여부 검증 필요
        return postRepository.findByUserId(userId, pageable).map(Post::from);
    }

    @Transactional(readOnly = true)
    public Page<Post> getMyPosts(User user, Pageable pageable) {
        return postRepository.findByUserId(user.getId(), pageable).map(Post::from);
    }

    @Transactional
    public void updatePost(
        Long postId,
        UpdatePostCommand updatePostCommand,
        User user
    ) {
        Post post = getById(postId);

        validatePostOwnership(user.getId(), post.getUser().getId());

        post.update(updatePostCommand);

        postRepository.save(PostEntity.from(post));
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getById(postId);

        validatePostOwnership(user.getId(), post.getUser().getId());

        postRepository.delete(PostEntity.from(post));
    }

    private void validatePostOwnership(Long userId, Long authorId) {
        if (!Objects.equals(userId, authorId)) {
            throw new UnauthorizedAccessException(AuthErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
