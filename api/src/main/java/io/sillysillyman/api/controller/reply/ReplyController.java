package io.sillysillyman.api.controller.reply;

import io.sillysillyman.core.domain.reply.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/comments/{commentId}/replies")
@RestController
public class ReplyController {

    private ReplyService replyService;
}
