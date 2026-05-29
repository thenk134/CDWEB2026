package com.news2026.controller;

import com.news2026.entity.Article;
import com.news2026.entity.Bookmark;
import com.news2026.entity.User;
import com.news2026.repository.ArticleRepository;
import com.news2026.repository.BookmarkRepository;
import com.news2026.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private Optional<User> authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return userService.getUserByToken(token);
    }

    @GetMapping
    public ResponseEntity<?> getBookmarks(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticate(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập!"));
        }

        User user = userOpt.get();
        List<Bookmark> bookmarks = bookmarkRepository.findByUserOrderBySavedDateDesc(user);
        
        // Trả về danh sách các bài viết tương ứng
        List<Article> articles = bookmarks.stream()
                .map(Bookmark::getArticle)
                .collect(Collectors.toList());

        return ResponseEntity.ok(articles);
    }

    @PostMapping
    public ResponseEntity<?> addBookmark(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> request) {
        
        Optional<User> userOpt = authenticate(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập!"));
        }

        User user = userOpt.get();
        Optional<Article> articleOpt = Optional.empty();

        if (request.containsKey("articleId")) {
            Long articleId = Long.valueOf(request.get("articleId").toString());
            articleOpt = articleRepository.findById(articleId);
        } else if (request.containsKey("link")) {
            String link = request.get("link").toString();
            articleOpt = articleRepository.findByLink(link);
        }

        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bài viết!"));
        }

        Article article = articleOpt.get();

        // Kiểm tra xem đã bookmark chưa
        if (bookmarkRepository.findByUserAndArticle(user, article).isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Bài viết này đã được lưu từ trước!"));
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setArticle(article);
        bookmark.setSavedDate(LocalDateTime.now());
        bookmarkRepository.save(bookmark);

        return ResponseEntity.ok(Map.of("message", "Lưu bài viết thành công!"));
    }

    @DeleteMapping("/{articleId}")
    @Transactional
    public ResponseEntity<?> removeBookmark(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long articleId) {
        
        Optional<User> userOpt = authenticate(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập!"));
        }

        User user = userOpt.get();
        Optional<Article> articleOpt = articleRepository.findById(articleId);

        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bài viết!"));
        }

        bookmarkRepository.deleteByUserAndArticle(user, articleOpt.get());
        return ResponseEntity.ok(Map.of("message", "Đã xóa khỏi danh sách lưu tin!"));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkBookmark(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String link) {
        
        Optional<User> userOpt = authenticate(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("bookmarked", false));
        }

        User user = userOpt.get();
        Optional<Article> articleOpt = Optional.empty();

        if (articleId != null) {
            articleOpt = articleRepository.findById(articleId);
        } else if (link != null && !link.trim().isEmpty()) {
            articleOpt = articleRepository.findByLink(link);
        }

        if (articleOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("bookmarked", false));
        }

        boolean isBookmarked = bookmarkRepository.findByUserAndArticle(user, articleOpt.get()).isPresent();
        return ResponseEntity.ok(Map.of("bookmarked", isBookmarked, "articleId", articleOpt.get().getId()));
    }
}
