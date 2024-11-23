package io.sillysillyman.core.domain.reply.repository;

import io.sillysillyman.core.domain.reply.ReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<ReplyEntity, Long> {

}
