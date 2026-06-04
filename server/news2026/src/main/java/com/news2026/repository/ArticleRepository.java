package com.news2026.repository;

import com.news2026.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByLink(String link);
    List<Article> findByCategoryOrderByPubDateDesc(String category);
    List<Article> findByCategoryAndSourceOrderByPubDateDesc(String category, String source);
    List<Article> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByPubDateDesc(String title, String description);
    List<Article> findAllByOrderByIdDesc();
    List<Article> findByCategoryAndStatusOrderByPubDateDesc(String category, int status);
    List<Article> findByCategoryAndSourceAndStatusOrderByPubDateDesc(String category, String source, int status);
    List<Article> findByCategoryAndStatusOrderByIdDesc(String category, int status);
    List<Article> findByStatusOrderByIdDesc(int status);
    // 4. Tìm kiếm công khai (Chỉ tìm bài đã duyệt)
    @Query("SELECT a FROM Article a WHERE a.status = :status AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Article> searchPublicArticles(@Param("query") String query, @Param("status") int status);
}
