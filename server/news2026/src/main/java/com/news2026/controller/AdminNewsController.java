package com.news2026.controller;

import com.news2026.entity.Article;
import com.news2026.entity.User;
import com.news2026.repository.ArticleRepository;
import com.news2026.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/news")
public class AdminNewsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleRepository articleRepository;

    private Optional<User> authenticateAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        Optional<User> userOpt = userService.getUserByToken(token);
        return userOpt.filter(user -> "ADMIN".equalsIgnoreCase(user.getRole()));
    }

    @GetMapping
    public ResponseEntity<?> getAllNews(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối! Bạn phải là Admin."));
        }

        List<Article> articles = articleRepository.findAllByOrderByIdDesc();
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        
        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bài viết!"));
        }

        return ResponseEntity.ok(articleOpt.get());
    }

    @PostMapping
    public ResponseEntity<?> createArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Article articleData) {
        
        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        if (articleData.getTitle() == null || articleData.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tiêu đề không được để trống!"));
        }

        Article article = new Article();
        article.setTitle(articleData.getTitle());
        article.setDescription(articleData.getDescription());
        article.setContent(articleData.getContent());
        article.setImage(articleData.getImage() != null && !articleData.getImage().isEmpty() ? 
                articleData.getImage() : "https://via.placeholder.com/400x250");
        article.setCategory(articleData.getCategory() != null ? articleData.getCategory() : "thoi-su");
        article.setSource("admin");
        article.setLocal(true);
        article.setExclusive(articleData.isExclusive());
        
        // Tạo link ngẫu nhiên cho bài viết nội bộ để đảm bảo tính duy nhất
        article.setLink("/news/local-" + UUID.randomUUID().toString());

        // Định dạng thời gian pubDate cho bài viết mới
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
        article.setPubDate(LocalDateTime.now().format(formatter));

        Article saved = articleRepository.save(article);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Article articleData) {
        
        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Optional<Article> existingOpt = articleRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bài viết!"));
        }

        Article article = existingOpt.get();
        if (articleData.getTitle() == null || articleData.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tiêu đề không được để trống!"));
        }

        article.setTitle(articleData.getTitle());
        article.setDescription(articleData.getDescription());
        article.setContent(articleData.getContent());
        if (articleData.getImage() != null && !articleData.getImage().isEmpty()) {
            article.setImage(articleData.getImage());
        }
        if (articleData.getCategory() != null) {
            article.setCategory(articleData.getCategory());
        }
        article.setExclusive(articleData.isExclusive());

        Article saved = articleRepository.save(article);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        
        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Optional<Article> existingOpt = articleRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bài viết!"));
        }

        articleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa bài viết thành công!"));
    }
    // Lấy danh sách bài viết "Quan điểm - Tranh luận" đang CHỜ DUYỆT (status = 0)
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUserPosts(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        // Lấy các bài viết thuộc danh mục mới và trạng thái = 0 (Chờ duyệt)
        List<Article> pendingPosts = articleRepository.findByStatusOrderByIdDesc(0);
        return ResponseEntity.ok(pendingPosts);
    }
    // Xử lý Xét duyệt (status = 1) hoặc Đánh dấu vi phạm (status = 2)
    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewPost(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authenticateAdmin(authHeader).isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Optional<Article> postOpt = articleRepository.findById(id);
        if (postOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy bài viết này!"));
        }

        Article post = postOpt.get();

        // Cập nhật trạng thái duyệt (1) hoặc vi phạm (2)
        post.setStatus(status);

        // FIX LỖI CRASH THỜI GIAN TẠI ĐÂY: Dùng Date() chuẩn của hệ thống
        if (status == 1) {
            post.setPubDate(new java.util.Date().toString());
        }

        articleRepository.save(post);

        String msg = (status == 1) ? "Xét duyệt và đăng bài thành công!" : "Đã đánh dấu vi phạm và ẩn bài viết!";
        return ResponseEntity.ok(Map.of("message", msg));
    }
}
