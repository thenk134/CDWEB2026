package com.news2026.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "read_log")
public class ReadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "read_date", nullable = false)
    private Date readDate;

    public ReadLog() {
        this.readDate = new Date();
    }

    public ReadLog(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
        this.readDate = new Date();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Date getReadDate() {
        return readDate;
    }

    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }
}
