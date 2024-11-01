package io.sillysillyman.socialmediabackend.domain.post.service;

import io.sillysillyman.socialmediabackend.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
}
