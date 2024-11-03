package io.sillysillyman.socialmediabackend.domain.comment;


import io.sillysillyman.socialmediabackend.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@RestController
public class CommentController {

    private final CommentService commentService;
}
