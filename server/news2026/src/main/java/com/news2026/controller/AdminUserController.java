package com.news2026.controller;

import com.news2026.entity.User;
import com.news2026.repository.UserRepository;
import com.news2026.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Helper method to authenticate Admin role
    private Optional<User> authenticateAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        Optional<User> userOpt = userService.getUserByToken(token);
        if (userOpt.isPresent() && "ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return userOpt;
        }
        return Optional.empty();
    }

    // Lấy toàn bộ danh sách người dùng
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> adminOpt = authenticateAdmin(authHeader);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Bạn không có quyền truy cập chức năng này!"));
        }

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (User u : users) {
            result.add(Map.of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "email", u.getEmail() != null ? u.getEmail() : "",
                    "role", u.getRole() != null ? u.getRole() : "USER",
                    "points", u.getPoints()
            ));
        }

        return ResponseEntity.ok(result);
    }

    // Cập nhật quyền hạn thành viên
    @PostMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        
        Optional<User> adminOpt = authenticateAdmin(authHeader);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Bạn không có quyền thực hiện hành động này!"));
        }

        String newRole = body.get("role");
        if (newRole == null || (!"USER".equalsIgnoreCase(newRole) && !"MEMBER".equalsIgnoreCase(newRole))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quyền hạn không hợp lệ!"));
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Không tìm thấy người dùng!"));
        }

        User user = userOpt.get();
        
        // Không được phép sửa quyền của tài khoản Admin
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể chỉnh sửa quyền hạn của quản trị viên!"));
        }

        user.setRole(newRole.toUpperCase());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật quyền hạn thành công!",
                "userId", user.getId(),
                "role", user.getRole()
        ));
    }
}
