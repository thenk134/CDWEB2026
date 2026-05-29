package com.news2026.repository;

import com.news2026.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByLink(String link);
    List<Article> findByCategoryOrderByPubDateDesc(String category);
    List<Article> findByCategoryAndSourceOrderByPubDateDesc(String category, String source);
    List<Article> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByPubDateDesc(String title, String description);
    List<Article> findAllByOrderByIdDesc();
}
