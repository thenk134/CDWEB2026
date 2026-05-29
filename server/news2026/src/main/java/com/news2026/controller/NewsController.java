package com.news2026.controller;

import com.news2026.entity.Article;
import com.news2026.repository.ArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("/news/home-laodong")
    public ResponseEntity<?> getHomeNews() {
        try {
            // Lấy các bài báo thuộc danh mục trang chủ "tin-moi-nhat"
            List<Article> articles = articleRepository.findByCategoryOrderByPubDateDesc("tin-moi-nhat");
            if (articles.isEmpty()) {
                // Nếu rỗng, lấy tạm 20 bài báo mới nhất trong hệ thống
                articles = articleRepository.findAllByOrderByIdDesc();
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
            List<Article> articles = articleRepository.findByCategoryAndSourceOrderByPubDateDesc(category, source);
            if (articles.isEmpty()) {
                // Nếu không tìm thấy theo nguồn, thử tìm tất cả các nguồn của danh mục đó
                articles = articleRepository.findByCategoryOrderByPubDateDesc(category);
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
            List<Article> articles = articleRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByPubDateDesc(query, query);
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi tìm kiếm: " + e.getMessage()));
        }
    }

    @GetMapping("/news-detail")
    public ResponseEntity<?> getNewsDetail(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu URL"));
        }

        // 1. Kiểm tra xem bài viết đã lưu trong database chưa
        Optional<Article> articleOpt = articleRepository.findByLink(url);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            // Nếu là bài viết nội bộ hoặc bài viết RSS đã được cào nội dung trước đó
            if (article.getContent() != null && !article.getContent().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "id", article.getId(),
                        "title", article.getTitle(),
                        "description", article.getDescription() != null ? article.getDescription() : "",
                        "content", article.getContent(),
                        "image", article.getImage() != null ? article.getImage() : "",
                        "link", article.getLink(),
                        "date", article.getPubDate() != null ? article.getPubDate() : "",
                        "source", article.getSource() != null ? article.getSource() : ""
                ));
            }
        }

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
            Article article;
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
            articleRepository.save(article);

            return ResponseEntity.ok(Map.of(
                    "id", article.getId(),
                    "title", article.getTitle(),
                    "description", article.getDescription() != null ? article.getDescription() : "",
                    "content", content,
                    "image", article.getImage() != null ? article.getImage() : "",
                    "link", url,
                    "date", article.getPubDate() != null ? article.getPubDate() : "",
                    "source", article.getSource() != null ? article.getSource() : ""
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "title", articleOpt.map(Article::getTitle).orElse("Lỗi tải bài viết"),
                    "description", articleOpt.map(Article::getDescription).orElse(""),
                    "content", "Không thể lấy nội dung từ nguồn này: " + e.getMessage()
            ));
        }
    }
}
