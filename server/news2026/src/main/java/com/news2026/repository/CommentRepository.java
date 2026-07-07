package com.news2026.repository;

import com.news2026.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdOrderByUpvotesDescCreatedDateDesc(Long articleId);
}
