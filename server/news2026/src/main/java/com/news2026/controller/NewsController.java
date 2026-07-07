package com.news2026.controller;

import com.news2026.entity.Article;
import com.news2026.entity.User;
import com.news2026.repository.ArticleRepository;
import com.news2026.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.news2026.entity.ReadLog;
import com.news2026.entity.Comment;
import com.news2026.entity.CommentVote;
import com.news2026.entity.PointTransaction;
import com.news2026.repository.ReadLogRepository;
import com.news2026.repository.CommentRepository;
import com.news2026.repository.CommentVoteRepository;
import com.news2026.repository.PointTransactionRepository;
import com.news2026.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ReadLogRepository readLogRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentVoteRepository commentVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    // kiểm tra token và lấy thông tin User/Member đăng nhập
    private Optional<User> authenticateUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return userService.getUserByToken(token);
    }

    @GetMapping("/news/home-laodong")
    public ResponseEntity<?> getHomeNews() {
        try {
            // Lấy các bài báo thuộc danh mục trang chủ "tin-moi-nhat"
            List<Article> articles = articleRepository.findByCategoryAndStatusOrderByPubDateDesc("tin-moi-nhat", 1);
            if (articles.isEmpty()) {
                // Nếu rỗng, lấy tạm 20 bài báo mới nhất trong hệ thống
                articles = articleRepository.findByStatusOrderByIdDesc(1);
                if (articles.size() > 20) {
                    articles = articles.subList(0, 20);
                }
            }
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/news/{source}/{category}")
    public ResponseEntity<?> getCategoryNews(@PathVariable String source, @PathVariable String category) {
        try {
            List<Article> articles = articleRepository.findByCategoryAndSourceAndStatusOrderByPubDateDesc(category, source, 1);
            if (articles.isEmpty()) {
                // Nếu không tìm thấy theo nguồn, thử tìm tất cả các nguồn của danh mục đó
                articles = articleRepository.findByCategoryAndStatusOrderByPubDateDesc(category, 1);
            }
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/news/search")
    public ResponseEntity<?> searchNews(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<Article> articles = articleRepository.searchPublicArticles(query, 1);
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi tìm kiếm: " + e.getMessage()));
        }
    }

    @GetMapping("/news-detail")
    public ResponseEntity<?> getNewsDetail(
            @RequestParam String url,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu URL"));
        }

        Article article = null;
        Optional<Article> articleOpt = articleRepository.findByLink(url);

        if (articleOpt.isPresent() && articleOpt.get().getContent() != null && !articleOpt.get().getContent().trim().isEmpty()) {
            article = articleOpt.get();
        } else {
            // 2. Nếu chưa có hoặc content rỗng (do RSS ban đầu chỉ có mô tả), thực hiện cào nội dung
            try {
                Document pageDoc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                        .timeout(15000)
                        .get();

                pageDoc.select("script, style, iframe, .box-comment, .box-ads, .relative-news, footer, header, nav").remove();

                Element titleEl = pageDoc.select("h1").first();
                String title = titleEl != null ? titleEl.text().trim() : "";

                Element descEl = pageDoc.select(".sapo, .description, .article-sapo, .sapo-detail, .article_sapo, h2").first();
                String description = descEl != null ? descEl.text().trim() : "";

                String[] contentSelectors = {
                        "article.fck_detail",     // VnExpress
                        ".article-content",       // Lao Động
                        "#main-detail-body",      // Tuổi Trẻ
                        ".content-news-detail",   // Người Lao Động
                        ".cms-body",              // Thanh Niên
                        ".post-content",          // Chung
                        "[itemprop=\"articleBody\"]" // Chuẩn SEO chung
                };

                Element mainContainer = null;
                for (String selector : contentSelectors) {
                    Element el = pageDoc.selectFirst(selector);
                    if (el != null) {
                        mainContainer = el;
                        break;
                    }
                }

                if (mainContainer == null) {
                    mainContainer = pageDoc.selectFirst("article");
                }
                if (mainContainer == null) {
                    mainContainer = pageDoc.body();
                }

                StringBuilder contentBuilder = new StringBuilder();
                if (mainContainer != null) {
                    Elements children = mainContainer.select("p, img");
                    for (Element child : children) {
                        if (child.normalName().equals("p")) {
                            String pText = child.text().trim();
                            if (pText.length() > 20) {
                                contentBuilder.append(String.format("<p class=\"mb-5 text-gray-800 leading-relaxed text-lg\">%s</p>", child.html()));
                            }
                        } else if (child.normalName().equals("img")) {
                            String src = child.attr("data-src");
                            if (src.isEmpty()) src = child.attr("src");
                            if (src.isEmpty()) src = child.attr("data-original");

                            if (!src.isEmpty() && !src.startsWith("data:")) {
                                if (src.startsWith("//")) {
                                    src = "https:" + src;
                                }
                                contentBuilder.append(String.format("<div class=\"my-6 text-center\"><img src=\"%s\" class=\"w-full rounded-lg shadow-md mx-auto\" alt=\"content image\" /></div>", src));
                            }
                        }
                    }
                }

                String content = contentBuilder.toString();
                if (content.isEmpty()) {
                    content = description.isEmpty() ? "Không thể tải được nội dung bài viết." : description;
                }

                // 3. Lưu nội dung vừa cào lại vào database để tăng tốc cho lần đọc sau
                if (articleOpt.isPresent()) {
                    article = articleOpt.get();
                } else {
                    article = new Article();
                    article.setTitle(title.isEmpty() ? "Tin tức" : title);
                    article.setDescription(description);
                    article.setLink(url);
                    article.setImage("https://via.placeholder.com/400x250");
                    article.setPubDate(new Date().toString());
                    article.setLocal(false);
                    article.setCategory("tin-moi-nhat");
                    article.setSource(url.contains("tuoitre.vn") ? "tuoitre" : "nld");
                }
                article.setContent(content);
                article = articleRepository.save(article);

            } catch (Exception e) {
                e.printStackTrace();
                if (articleOpt.isPresent()) {
                    article = articleOpt.get();
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "Lỗi tải bài viết: " + e.getMessage()));
                }
            }
        }

        // Tăng lượt xem (views)
        article.setViews(article.getViews() + 1);

        // Cộng điểm thưởng nhuận bút cho tác giả hội viên
        if (article.isLocal() && !"admin".equalsIgnoreCase(article.getSource())) {
            Optional<User> authorOpt = userRepository.findByUsername(article.getSource());
            if (authorOpt.isPresent()) {
                User author = authorOpt.get();
                if (article.getViews() % 10 == 0) {
                    author.setPoints(Math.round((author.getPoints() + 1.0) * 10.0) / 10.0);
                    userRepository.save(author);
                    pointTransactionRepository.save(new PointTransaction(
                            author.getId(),
                            1.0,
                            "Bài viết '" + article.getTitle() + "' đạt mốc " + article.getViews() + " lượt xem"
                    ));
                }
            }
        }

        article = articleRepository.save(article);

        // Logic Paywall: Kiểm duyệt quyền đọc tin
        boolean isLocked = false;
        long freeTrialsLeft = -1;

        if (article.isExclusive()) {
            Optional<User> userOpt = authenticateUser(authHeader);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if ("MEMBER".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole())) {
                    isLocked = false;
                    freeTrialsLeft = 999;
                } else { // Tài khoản thường USER
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Date startOfMonth = cal.getTime();

                    long readCount = readLogRepository.countDistinctArticlesReadSince(user.getId(), startOfMonth);
                    Optional<ReadLog> existingLog = readLogRepository.findByUserIdAndArticleId(user.getId(), article.getId());

                    if (existingLog.isPresent()) {
                        isLocked = false;
                        freeTrialsLeft = 3 - readCount;
                    } else {
                        if (readCount < 3) {
                            readLogRepository.save(new ReadLog(user.getId(), article.getId()));
                            isLocked = false;
                            freeTrialsLeft = 3 - (readCount + 1);
                        } else {
                            isLocked = true;
                            freeTrialsLeft = 0;
                        }
                    }
                }
            } else {
                // Khách chưa đăng nhập
                isLocked = true;
                freeTrialsLeft = -1;
            }
        }

        Long authorId = null;
        if (article.isLocal() && !"admin".equalsIgnoreCase(article.getSource())) {
            Optional<User> authorOpt = userRepository.findByUsername(article.getSource());
            if (authorOpt.isPresent()) {
                authorId = authorOpt.get().getId();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", article.getId());
        response.put("title", article.getTitle());
        response.put("description", article.getDescription() != null ? article.getDescription() : "");
        response.put("content", isLocked ? "" : article.getContent());
        response.put("image", article.getImage() != null ? article.getImage() : "");
        response.put("link", article.getLink());
        response.put("date", article.getPubDate() != null ? article.getPubDate() : "");
        response.put("source", article.getSource() != null ? article.getSource() : "");
        response.put("exclusive", article.isExclusive());
        response.put("isExclusive", article.isExclusive());
        response.put("isLocked", isLocked);
        response.put("freeTrialsLeft", freeTrialsLeft);
        response.put("authorId", authorId);

        return ResponseEntity.ok(response);
    }
    // Đăng bài viết mới dành cho Member (Ép cứng status = 0 và mục Quan điểm)

    @PostMapping("/news/my-posts")
    public ResponseEntity<?> createMemberPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Article articleData) {

        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Bạn cần đăng nhập trước khi thực hiện chức năng này!"));
        }
        User user = userOpt.get();

        if (articleData.getTitle() == null || articleData.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tiêu đề không được để trống!"));
        }

        Article article = new Article();
        article.setTitle(articleData.getTitle());
        article.setDescription(articleData.getDescription());
        article.setContent(articleData.getDescription()); // Đồng bộ nội dung bài viết
        article.setImage(articleData.getImage() != null && !articleData.getImage().isEmpty() ?
                articleData.getImage() : "https://via.placeholder.com/400x250");

        article.setCategory("quandiem-tranhluan");
        article.setStatus(0);                    // Trạng thái Chờ duyệt bắt buộc
        article.setSource(user.getUsername());
        article.setLocal(true);

        // Tạo link ngẫu nhiên bảo mật tránh trùng lặp tin tức
        article.setLink("/news/local-" + UUID.randomUUID().toString());

        // định dạng giờ an toàn
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
        article.setPubDate(java.time.ZonedDateTime.now().format(formatter));

        Article saved = articleRepository.save(article);
        return ResponseEntity.ok(saved);
    }
    //Lấy danh sách bài viết của riêng cá nhân Member

    @GetMapping("/news/my-posts")
    public ResponseEntity<?> getMyPosts(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập!"));
        }
        User user = userOpt.get();

        // Lấy tất cả bài viết nội bộ và lọc ra bài do chính User này viết (Dựa trên trường Source)
        List<Article> allArticles = articleRepository.findAllByOrderByIdDesc();
        List<Article> myArticles = allArticles.stream()
                .filter(article -> user.getUsername().equalsIgnoreCase(article.getSource()))
                .toList();

        return ResponseEntity.ok(myArticles);
    }
    /**
     * API 3: Lấy chi tiết bài viết cá nhân bằng ID (Dành cho việc đổ dữ liệu lên form Chỉnh sửa ở Frontend)
     * Đường dẫn: GET http://localhost:5000/api/news/my-posts/{id}
     */
    @GetMapping("/news/my-posts/{id}")
    public ResponseEntity<?> getMyPostById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập trước!"));
        }
        User user = userOpt.get();

        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bài viết không tồn tại trên hệ thống."));
        }

        Article article = articleOpt.get();

        // KIỂM TRA BẢO MẬT: Người yêu cầu xem/sửa phải trùng tên Username với tác giả (trường Source) của bài viết
        if (!user.getUsername().equalsIgnoreCase(article.getSource())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Bạn không có quyền can thiệp vào bài viết của người khác!"));
        }

        return ResponseEntity.ok(article);
    }

    /**
     * Cập nhật nội dung sửa đổi bài viết của Member (Tự động đưa status về 0)
     * PUT http://localhost:5000/api/news/my-posts/{id}
     */
    @PutMapping("/news/my-posts/{id}")
    public ResponseEntity<?> updateMemberPost(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Article updatedData) {

        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập hệ thống!"));
        }
        User user = userOpt.get();

        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bài viết cần sửa không tồn tại."));
        }

        Article existingArticle = articleOpt.get();

        // không cho sửa bài viết của thành viên khác
        if (!user.getUsername().equalsIgnoreCase(existingArticle.getSource())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Hành động bị cấm! Bạn không sở hữu bài viết này."));
        }

        // ghi nhận thông tin chỉnh sửa mới
        existingArticle.setTitle(updatedData.getTitle());
        existingArticle.setDescription(updatedData.getDescription());
        existingArticle.setContent(updatedData.getDescription()); // Đồng bộ trường Content
        if (updatedData.getImage() != null && !updatedData.getImage().isEmpty()) {
            existingArticle.setImage(updatedData.getImage());
        }

        // ĐƯA TRẠNG THÁI VỀ 0 để Admin thấy
        existingArticle.setStatus(0);

        // CẬP NHẬT THỜI GIAN
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
        existingArticle.setPubDate(java.time.ZonedDateTime.now().format(formatter));

        Article saved = articleRepository.save(existingArticle);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/news/my-posts/{id}")
    public ResponseEntity<?> deleteMemberPost(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Yêu cầu đăng nhập!"));
        }
        User user = userOpt.get();

        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bài viết không tồn tại."));
        }

        Article article = articleOpt.get();

        // Chỉ cho phép xóa bài của chính mình viết
        if (!user.getUsername().equalsIgnoreCase(article.getSource())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Bạn không được phép xóa bài viết của người khác!"));
        }

        // xóa khỏi Database thông qua JPA Repository
        articleRepository.delete(article);
        return ResponseEntity.ok(Map.of("message", "Đã gỡ bỏ bài viết thành công!"));
    }

    @GetMapping("/news/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        List<Comment> allComments = commentRepository.findByArticleIdOrderByUpvotesDescCreatedDateDesc(id);
        List<Map<String, Object>> agree = new ArrayList<>();
        List<Map<String, Object>> disagree = new ArrayList<>();
        
        for (Comment c : allComments) {
            Map<String, Object> cMap = new HashMap<>();
            cMap.put("id", c.getId());
            cMap.put("content", c.getContent());
            cMap.put("upvotes", c.getUpvotes());
            cMap.put("createdDate", c.getCreatedDate());
            cMap.put("isAgree", c.isAgree());
            cMap.put("username", c.getUser() != null ? c.getUser().getUsername() : "Ẩn danh");
            cMap.put("role", c.getUser() != null ? c.getUser().getRole() : "USER");
            if (c.isAgree()) {
                agree.add(cMap);
            } else {
                disagree.add(cMap);
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "agree", agree,
                "disagree", disagree
        ));
    }

    @PostMapping("/news/{id}/comments")
    public ResponseEntity<?> postComment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Bạn cần đăng nhập để tham gia bình luận!"));
        }
        User user = userOpt.get();
        if (!"MEMBER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Chức năng tranh luận chỉ dành riêng cho Hội viên!"));
        }
        
        String content = (String) body.get("content");
        Boolean isAgree = (Boolean) body.get("isAgree");
        
        if (content == null || content.trim().isEmpty() || isAgree == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nội dung bình luận không hợp lệ!"));
        }

        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bài viết không tồn tại!"));
        }
        Article article = articleOpt.get();
        
        boolean hasAlreadyCommented = commentRepository.existsByArticleIdAndUserId(id, user.getId());
        
        Comment comment = new Comment(id, user.getId(), content, isAgree);
        Comment saved = commentRepository.save(comment);
        
        // Cộng/trừ điểm nhuận bút cho tác giả bài viết ngay khi đăng luận điểm tranh luận
        if (article.isLocal() && !"admin".equalsIgnoreCase(article.getSource())) {
            Optional<User> authorOpt = userRepository.findByUsername(article.getSource());
            if (authorOpt.isPresent()) {
                User author = authorOpt.get();
                boolean isAuthorCommenting = author.getId().equals(user.getId());
                
                // CHỈ TÍNH ĐIỂM khi: Không phải tác giả tự bình luận VÀ đây là bình luận đầu tiên của độc giả này trên bài viết
                if (!isAuthorCommenting && !hasAlreadyCommented) {
                    if (isAgree) {
                        author.setPoints(Math.round((author.getPoints() + 1.0) * 10.0) / 10.0);
                        userRepository.save(author);
                        pointTransactionRepository.save(new PointTransaction(
                                author.getId(),
                                1.0,
                                "Bài viết '" + article.getTitle() + "' nhận ý kiến đồng ý đầu tiên từ @" + user.getUsername()
                        ));
                    } else {
                        author.setPoints(Math.round((author.getPoints() - 0.7) * 10.0) / 10.0);
                        userRepository.save(author);
                        pointTransactionRepository.save(new PointTransaction(
                                author.getId(),
                                -0.7,
                                "Bài viết '" + article.getTitle() + "' nhận ý kiến phản đối đầu tiên từ @" + user.getUsername()
                        ));
                    }
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "message", "Đã gửi bình luận tranh luận thành công!",
                "comment", Map.of(
                        "id", saved.getId(),
                        "content", saved.getContent(),
                        "upvotes", saved.getUpvotes(),
                        "createdDate", saved.getCreatedDate(),
                        "isAgree", saved.isAgree(),
                        "username", user.getUsername(),
                        "role", user.getRole()
                )
        ));
    }

    @PostMapping("/comments/{id}/vote")
    public ResponseEntity<?> voteComment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Bạn cần đăng nhập để ủng hộ lập luận này!"));
        }
        User user = userOpt.get();
        
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bình luận không tồn tại!"));
        }
        
        Comment comment = commentOpt.get();
        Optional<CommentVote> existingVote = commentVoteRepository.findByCommentIdAndUserId(comment.getId(), user.getId());
        
        if (existingVote.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã ủng hộ luận điểm này rồi!"));
        }
        
        commentVoteRepository.save(new CommentVote(comment.getId(), user.getId()));
        comment.setUpvotes(comment.getUpvotes() + 1);
        commentRepository.save(comment);
        
        // Cộng điểm thưởng nhuận bút cho tác giả bình luận theo quy tắc đồng ý (+1.0) / phản đối (-0.7)
        Optional<User> authorOpt = userRepository.findById(comment.getUserId());
        if (authorOpt.isPresent()) {
            User author = authorOpt.get();
            if (comment.isAgree()) {
                author.setPoints(Math.round((author.getPoints() + 1.0) * 10.0) / 10.0);
                userRepository.save(author);
                pointTransactionRepository.save(new PointTransaction(
                        author.getId(),
                        1.0,
                        "Luận điểm đồng ý của bạn được ủng hộ bởi @" + user.getUsername()
                ));
            } else {
                author.setPoints(Math.round((author.getPoints() - 0.7) * 10.0) / 10.0);
                userRepository.save(author);
                pointTransactionRepository.save(new PointTransaction(
                        author.getId(),
                        -0.7,
                        "Luận điểm phản đối/ý kiến khác của bạn được ủng hộ bởi @" + user.getUsername()
                ));
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "message", "Đã ủng hộ luận điểm thành công!",
                "upvotes", comment.getUpvotes()
        ));
    }

    @GetMapping("/news/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        List<User> allUsers = userRepository.findAll();
        List<User> members = allUsers.stream()
                .filter(u -> "MEMBER".equalsIgnoreCase(u.getRole()))
                .sorted((u1, u2) -> Double.compare(u2.getPoints(), u1.getPoints()))
                .limit(5)
                .toList();
                
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : members) {
            result.add(Map.of(
                    "username", u.getUsername(),
                    "points", u.getPoints()
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/members/{username}")
    public ResponseEntity<?> getMemberProfile(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy thành viên này!"));
        }
        User user = userOpt.get();

        Map<String, Object> publicProfile = new HashMap<>();
        publicProfile.put("id", user.getId());
        publicProfile.put("username", user.getUsername());
        publicProfile.put("role", user.getRole() != null ? user.getRole() : "USER");
        publicProfile.put("points", user.getPoints());

        List<Article> articles = articleRepository.findBySourceAndStatusOrderByPubDateDesc(user.getUsername(), 1);

        return ResponseEntity.ok(Map.of(
                "profile", publicProfile,
                "articles", articles
        ));
    }
}
