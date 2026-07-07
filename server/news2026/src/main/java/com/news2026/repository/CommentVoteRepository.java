package com.news2026.repository;

import com.news2026.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentIdAndUserId(Long commentId, Long userId);
}
