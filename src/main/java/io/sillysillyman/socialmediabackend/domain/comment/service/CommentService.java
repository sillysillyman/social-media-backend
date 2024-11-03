package io.sillysillyman.socialmediabackend.domain.comment.service;

import io.sillysillyman.socialmediabackend.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
}
