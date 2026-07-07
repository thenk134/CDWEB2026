package com.news2026.controller;

import com.news2026.entity.PaymentOrder;
import com.news2026.entity.User;
import com.news2026.repository.PaymentOrderRepository;
import com.news2026.repository.UserRepository;
import com.news2026.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // API Khởi tạo đơn thanh toán
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Thiếu hoặc sai định dạng token!"));
        }

        String token = authHeader.substring(7);
        Optional<User> userOpt = userService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token không hợp lệ hoặc đã hết hạn!"));
        }

        User user = userOpt.get();

        // Định giá gói Hội viên cố định: $2.00 USD
        double amount = 2.00;
        
        // Tạo mã hóa giao dịch PayPal độc nhất
        String uniqueId = UUID.randomUUID().toString().substring(0, 5);
        String description = "PAYPAL_U" + user.getId() + "_O" + uniqueId;

        PaymentOrder order = new PaymentOrder(user.getId(), amount, description);
        PaymentOrder savedOrder = paymentOrderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "orderId", savedOrder.getId(),
                "amount", savedOrder.getAmount(),
                "description", savedOrder.getDescription(),
                "status", savedOrder.getStatus()
        ));
    }

    // API Kiểm tra trạng thái đơn thanh toán
    @GetMapping("/status/{id}")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long id) {
        Optional<PaymentOrder> orderOpt = paymentOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Đơn hàng không tồn tại!"));
        }
        return ResponseEntity.ok(Map.of("status", orderOpt.get().getStatus()));
    }

    // API Giả lập thanh toán chuyển khoản thành công
    @PostMapping("/simulate-success/{id}")
    public ResponseEntity<?> simulatePaymentSuccess(@PathVariable Long id) {
        Optional<PaymentOrder> orderOpt = paymentOrderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Đơn hàng không tồn tại!"));
        }

        PaymentOrder order = orderOpt.get();
        if ("PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            paymentOrderRepository.save(order);

            // Nâng cấp quyền người dùng thành MEMBER
            Optional<User> userOpt = userRepository.findById(order.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole("MEMBER");
                userRepository.save(user);
            }
            return ResponseEntity.ok(Map.of("message", "Thanh toán thành công và tài khoản đã được nâng cấp thành Hội viên!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Đơn hàng đã được thanh toán hoặc hết hạn trước đó!"));
        }
    }
}
