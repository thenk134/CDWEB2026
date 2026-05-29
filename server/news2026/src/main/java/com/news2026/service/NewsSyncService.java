package com.news2026.service;

import com.news2026.entity.Article;
import com.news2026.repository.ArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class NewsSyncService {

    @Autowired
    private ArticleRepository articleRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Chạy đồng bộ trong thread mới để không làm chậm lúc startup server
        new Thread(this::syncAllFeeds).start();
    }

    public void syncAllFeeds() {
        System.out.println("🚀 Bắt đầu đồng bộ tin tức từ RSS vào PostgreSQL...");
        
        String[] categories = {"thoi-su", "viec-lam", "phap-luat", "bao-hiem", "cong-doan", "suc-khoe"};

        // Đồng bộ tin trang chủ (tin mới nhất)
        syncFeed("https://tuoitre.vn/rss/tin-moi-nhat.rss", "tuoitre", "tin-moi-nhat");
        
        // Đồng bộ các danh mục của Tuổi Trẻ và Người Lao Động
        for (String category : categories) {
            syncFeed("https://tuoitre.vn/rss/" + category + ".rss", "tuoitre", category);
            syncFeed("https://nld.com.vn/" + category + ".rss", "nld", category);
        }
        System.out.println("✅ Đồng bộ tin tức hoàn tất!");
    }

    private void syncFeed(String rssUrl, String source, String category) {
        try {
            Document doc = Jsoup.connect(rssUrl)
                    .parser(Parser.xmlParser())
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements items = doc.select("item");
            int count = 0;
            for (Element item : items) {
                String title = item.select("title").text();
                String link = item.select("link").text();
                String pubDate = item.select("pubDate").text();

                if (link.isEmpty()) continue;

                // Kiểm tra xem bài viết đã tồn tại chưa
                if (articleRepository.findByLink(link).isPresent()) {
                    continue;
                }

                String descHtml = item.select("description").text();
                Document descDoc = Jsoup.parse(descHtml);
                String description = descDoc.text();
                if (description.isEmpty()) {
                    description = "Nhấp để xem chi tiết...";
                }

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

                Article article = new Article();
                article.setTitle(title);
                article.setLink(link);
                article.setPubDate(pubDate);
                article.setDescription(description);
                article.setImage(image);
                article.setCategory(category);
                article.setSource(source);
                article.setLocal(false);
                article.setContent(""); // Sẽ được crawl chi tiết khi người dùng nhấp vào đọc

                articleRepository.save(article);
                count++;
                if (count >= 15) { // Giới hạn 15 tin mỗi danh mục/nguồn để tối ưu hóa ban đầu
                    break;
                }
            }
            System.out.println("-> [Sync] Thành công " + count + " bài viết từ " + source + " / " + category);
        } catch (Exception e) {
            System.err.println("-> [Sync] Lỗi khi đồng bộ " + rssUrl + ": " + e.getMessage());
        }
    }
}
