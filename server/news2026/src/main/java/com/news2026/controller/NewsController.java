package com.news2026.controller;

import com.news2026.util.SimpleCache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class NewsController {

    private final SimpleCache<String, List<Map<String, Object>>> homeCache = new SimpleCache<>(300);
    private final SimpleCache<String, List<Map<String, Object>>> categoryCache = new SimpleCache<>(300);
    private final SimpleCache<String, Map<String, String>> detailCache = new SimpleCache<>(300);

    @GetMapping("/news/home-laodong")
    public ResponseEntity<?> getHomeNews() {
        String cacheKey = "home_news";
        List<Map<String, Object>> cached = homeCache.get(cacheKey);
        if (cached != null) {
            System.out.println("Lấy từ cache (Home)");
            return ResponseEntity.ok(cached);
        }

        try {
            String rssUrl = "https://tuoitre.vn/rss/tin-moi-nhat.rss";
            Document doc = Jsoup.connect(rssUrl)
                    .parser(Parser.xmlParser())
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            List<Map<String, Object>> articles = new ArrayList<>();
            Elements items = doc.select("item");

            for (Element item : items) {
                String title = item.select("title").text();
                String link = item.select("link").text();
                String pubDate = item.select("pubDate").text();
                String guid = item.select("guid").text();
                if (guid.isEmpty()) {
                    guid = link;
                }

                String descHtml = item.select("description").text();
                Document descDoc = Jsoup.parse(descHtml);
                String description = descDoc.text();
                if (description.isEmpty()) {
                    description = "Nhấp để xem chi tiết...";
                }

                String image = descDoc.select("img").attr("src");
                if (image.isEmpty()) {
                    image = "https://via.placeholder.com/400x250";
                }

                Map<String, Object> article = new HashMap<>();
                article.put("id", guid);
                article.put("title", title);
                article.put("description", description);
                article.put("link", link);
                article.put("date", pubDate);
                article.put("image", image);

                articles.add(article);
            }

            homeCache.put(cacheKey, articles);
            return ResponseEntity.ok(articles);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/news/{source}/{category}")
    public ResponseEntity<?> getCategoryNews(@PathVariable String source, @PathVariable String category) {
        String cacheKey = String.format("category_%s_%s", source, category);
        List<Map<String, Object>> cached = categoryCache.get(cacheKey);
        if (cached != null) {
            System.out.println(String.format("[Category: %s] Lấy từ cache", category));
            return ResponseEntity.ok(cached);
        }

        try {
            String rssUrl;
            if ("tuoitre".equals(source)) {
                rssUrl = "https://tuoitre.vn/rss/" + category + ".rss";
            } else if ("nld".equals(source)) {
                rssUrl = "https://nld.com.vn/" + category + ".rss";
            } else {
                rssUrl = "https://vnexpress.net/rss/" + category + ".rss";
            }

            System.out.println("🔥 Đang lấy tin từ: " + rssUrl);

            Document doc = Jsoup.connect(rssUrl)
                    .parser(Parser.xmlParser())
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            List<Map<String, Object>> articles = new ArrayList<>();
            Elements items = doc.select("item");

            for (Element item : items) {
                String title = item.select("title").text();
                String link = item.select("link").text();
                String pubDate = item.select("pubDate").text();

                String descHtml = item.select("description").text();
                Document descDoc = Jsoup.parse(descHtml);
                String description = descDoc.text();

                String image = "";
                Element enclosure = item.selectFirst("enclosure");
                if (enclosure != null) {
                    image = enclosure.attr("url");
                }
                if (image.isEmpty()) {
                    image = descDoc.select("img").attr("src");
                }
                if (image.isEmpty()) {
                    image = "https://via.placeholder.com/400x250";
                }

                Map<String, Object> article = new HashMap<>();
                article.put("title", title);
                article.put("link", link);
                article.put("date", pubDate);
                article.put("image", image);
                article.put("description", description);

                articles.add(article);
            }

            categoryCache.put(cacheKey, articles);
            return ResponseEntity.ok(articles);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/news-detail")
    public ResponseEntity<?> getNewsDetail(@RequestParam String url) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu URL"));
        }

        String cacheKey = "detail_" + url;
        Map<String, String> cached = detailCache.get(cacheKey);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

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

            Map<String, String> result = new HashMap<>();
            result.put("title", title);
            result.put("description", description);
            result.put("content", contentBuilder.toString());

            detailCache.put(cacheKey, result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "title", "Lỗi tải bài viết",
                    "description", "",
                    "content", "Không thể lấy nội dung từ nguồn này: " + e.getMessage()
            ));
        }
    }
}
