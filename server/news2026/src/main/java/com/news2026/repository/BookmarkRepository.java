package com.news2026.repository;

import com.news2026.entity.Bookmark;
import com.news2026.entity.User;
import com.news2026.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserOrderBySavedDateDesc(User user);
    Optional<Bookmark> findByUserAndArticle(User user, Article article);
    void deleteByUserAndArticle(User user, Article article);
}
