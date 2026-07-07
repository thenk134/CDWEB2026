package com.news2026.service;

import com.news2026.entity.User;
import com.news2026.repository.UserRepository;
import com.news2026.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String username, String email, String password) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(password));

        // Tự động phân quyền ADMIN nếu username là admin, còn lại là USER
        if ("admin".equalsIgnoreCase(username)) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    public User loginUser(String username, String password) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Tên đăng nhập hoặc mật khẩu không chính xác!"));

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new Exception("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        // Tạo token phiên đăng nhập ngẫu nhiên
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        return userRepository.save(user);
    }

    public Optional<User> getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByToken(token);
    }

    public void logoutUser(String token) {
        getUserByToken(token).ifPresent(user -> {
            user.setToken(null);
            userRepository.save(user);
        });
    }

    public String generateResetToken(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Không tìm thấy tài khoản với email này!"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);

        return resetToken;
    }

    public void resetPassword(String resetToken, String newPassword) throws Exception {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new Exception("Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn!"));

        user.setPassword(PasswordUtil.hashPassword(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User upgradeToMember(User user) {
        user.setRole("MEMBER");
        return userRepository.save(user);
    }
}
