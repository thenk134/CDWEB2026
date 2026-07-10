package com.news2026;

import com.news2026.entity.User;
import com.news2026.repository.UserRepository;
import com.news2026.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.news2026.repository.ArticleRepository articleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Seeding admin account (username: admin, password: admin)
        seedUser("admin", "admin@news2026.com", "admin", "ADMIN", 0.0);

        // Seeding test accounts with different points (password: test12)
        seedUser("test1", "test1@news2026.com", "test12", "USER", 0.0);
        seedUser("test2", "test2@news2026.com", "test12", "MEMBER", 50.0);
        seedUser("test3", "test3@news2026.com", "test12", "USER", 15.5);
        seedUser("test4", "test4@news2026.com", "test12", "MEMBER", 250.0);
        seedUser("test5", "test5@news2026.com", "test12", "MEMBER", 1000.0);
        
        System.out.println(">>> Database seeding completed successfully!");

        // Run a background thread to set some articles as exclusive for the VIP demo
        new Thread(() -> {
            try {
                // Wait 8 seconds for RSS feed sync to fetch articles
                Thread.sleep(8000);
                var articles = articleRepository.findAll();
                int count = 0;
                for (var article : articles) {
                    if (!article.isExclusive() && count < 5) {
                        article.setExclusive(true);
                        articleRepository.save(article);
                        count++;
                    }
                }
                System.out.println(">>> Configured " + count + " articles as EXCLUSIVE/VIP for testing.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println(">>> Error configuring exclusive articles: " + e.getMessage());
            }
        }).start();
    }

    private void seedUser(String username, String email, String rawPassword, String role, double points) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(PasswordUtil.hashPassword(rawPassword));
            user.setRole(role);
            user.setPoints(points);
            if ("MEMBER".equals(role)) {
                user.setVipExpireDate(java.time.LocalDate.now().plusMonths(1));
            }
            userRepository.save(user);
            System.out.println(">>> Seeded user: " + username + " with role: " + role + ", points: " + points);
        } else {
            // Update password, role & points to match instructions just in case
            User user = existingUser.get();
            user.setPassword(PasswordUtil.hashPassword(rawPassword));
            user.setRole(role);
            user.setPoints(points);
            if ("MEMBER".equals(role) && user.getVipExpireDate() == null) {
                user.setVipExpireDate(java.time.LocalDate.now().plusMonths(1));
            }
            userRepository.save(user);
            System.out.println(">>> Updated existing seed user: " + username + " with role: " + role + ", points: " + points);
        }
    }
}
