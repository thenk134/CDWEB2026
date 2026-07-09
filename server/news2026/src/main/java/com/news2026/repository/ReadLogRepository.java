package com.news2026.repository;

import com.news2026.entity.ReadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.Optional;

public interface ReadLogRepository extends JpaRepository<ReadLog, Long> {
    
    Optional<ReadLog> findByUserIdAndArticleId(Long userId, Long articleId);

    @Query("SELECT COUNT(DISTINCT r.articleId) FROM ReadLog r WHERE r.userId = :userId AND r.readDate >= :startDate")
    long countDistinctArticlesReadSince(@Param("userId") Long userId, @Param("startDate") Date startDate);
}
