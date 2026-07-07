package com.news2026.controller;

import com.news2026.entity.*;
import com.news2026.repository.*;
import com.news2026.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PointsController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PayoutRequestRepository payoutRequestRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    private Optional<User> authenticateUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return userService.getUserByToken(token);
    }

    // 1. Lấy lịch sử tích lũy điểm nhuận bút của bản thân
    @GetMapping("/points/history")
    public ResponseEntity<?> getPointHistory(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Đã hết phiên đăng nhập!"));
        }
        User user = userOpt.get();
        List<PointTransaction> txs = pointTransactionRepository.findByUserIdOrderByIdDesc(user.getId());
        return ResponseEntity.ok(txs);
    }

    // 2. Quy đổi điểm nhuận bút sang Hội Viên VIP (10 điểm = 7 ngày)
    @PostMapping("/points/redeem-vip")
    public ResponseEntity<?> redeemVip(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Đã hết phiên đăng nhập!"));
        }
        User user = userOpt.get();
        if (user.getPoints() < 10.0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn cần tối thiểu 10.0 điểm nhuận bút để quy đổi!"));
        }

        // Khấu trừ điểm
        user.setPoints(Math.round((user.getPoints() - 10.0) * 10.0) / 10.0);
        user.setRole("MEMBER");

        // Cộng dồn hoặc đặt mới ngày hết hạn VIP
        java.time.LocalDate now = java.time.LocalDate.now();
        if (user.getVipExpireDate() == null || user.getVipExpireDate().isBefore(now)) {
            user.setVipExpireDate(now.plusDays(7));
        } else {
            user.setVipExpireDate(user.getVipExpireDate().plusDays(7));
        }
        userRepository.save(user);

        // Lưu nhật ký giao dịch điểm
        pointTransactionRepository.save(new PointTransaction(
                user.getId(),
                -10.0,
                "Quy đổi 10 điểm sang 7 ngày Hội Viên VIP. VIP hạn dùng mới: " + user.getVipExpireDate()
        ));

        return ResponseEntity.ok(Map.of(
                "message", "Quy đổi VIP thành công! Đã gia hạn thêm 7 ngày Hội Viên.",
                "points", user.getPoints(),
                "role", user.getRole(),
                "vipExpireDate", user.getVipExpireDate().toString()
        ));
    }

    // 3. Yêu cầu rút tiền nhuận bút (1 điểm = 10,000 VND, min 10 điểm)
    @PostMapping("/points/payout-request")
    public ResponseEntity<?> requestPayout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Đã hết phiên đăng nhập!"));
        }
        User user = userOpt.get();

        double requestedPoints;
        try {
            requestedPoints = Double.parseDouble(body.get("points").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số điểm cần rút không hợp lệ!"));
        }

        if (requestedPoints < 10.0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số điểm quy đổi rút tiền tối thiểu là 10.0 điểm!"));
        }

        if (user.getPoints() < requestedPoints) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số dư điểm nhuận bút của bạn không đủ để thực hiện yêu cầu này!"));
        }

        String method = (String) body.get("payoutMethod");
        String info = (String) body.get("payoutInfo");

        if (method == null || method.trim().isEmpty() || info == null || info.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thông tin thanh toán không được để trống!"));
        }

        // Tỷ lệ quy đổi: 1 điểm = 10,000 VND
        double amountMoney = requestedPoints * 10000.0;

        // Khấu trừ điểm của người dùng ngay lập tức
        user.setPoints(Math.round((user.getPoints() - requestedPoints) * 10.0) / 10.0);
        userRepository.save(user);

        // Lưu yêu cầu
        PayoutRequest request = new PayoutRequest(user.getId(), requestedPoints, amountMoney, method, info);
        payoutRequestRepository.save(request);

        // Lưu nhật ký trừ điểm
        pointTransactionRepository.save(new PointTransaction(
                user.getId(),
                -requestedPoints,
                "Đăng ký rút tiền nhuận bút qua " + method + " (" + info + "). Chờ phê duyệt."
        ));

        return ResponseEntity.ok(Map.of(
                "message", "Gửi yêu cầu rút tiền thành công! Vui lòng chờ kiểm duyệt.",
                "points", user.getPoints()
        ));
    }

    // 4. Lấy lịch sử yêu cầu rút tiền của riêng mình
    @GetMapping("/points/payout-requests")
    public ResponseEntity<?> getMyPayoutRequests(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Đã hết phiên đăng nhập!"));
        }
        User user = userOpt.get();
        List<PayoutRequest> requests = payoutRequestRepository.findByUserIdOrderByIdDesc(user.getId());
        return ResponseEntity.ok(requests);
    }

    // 5. Admin: Lấy toàn bộ yêu cầu rút tiền
    @GetMapping("/admin/payout-requests")
    public ResponseEntity<?> getAllPayoutRequests(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        List<PayoutRequest> reqs = payoutRequestRepository.findAllByOrderByIdDesc();
        List<Map<String, Object>> result = reqs.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("userId", r.getUserId());
            map.put("points", r.getPoints());
            map.put("amountMoney", r.getAmountMoney());
            map.put("payoutMethod", r.getPayoutMethod());
            map.put("payoutInfo", r.getPayoutInfo());
            map.put("status", r.getStatus());
            map.put("createdAt", r.getCreatedAt());
            map.put("resolvedAt", r.getResolvedAt());
            
            // Tìm username tương ứng
            String username = userRepository.findById(r.getUserId())
                    .map(User::getUsername)
                    .orElse("Unknown");
            map.put("username", username);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 6. Admin: Phê duyệt rút tiền thành công
    @PostMapping("/admin/payout-requests/{id}/approve")
    public ResponseEntity<?> approvePayoutRequest(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Optional<PayoutRequest> reqOpt = payoutRequestRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Yêu cầu rút tiền không tồn tại!"));
        }

        PayoutRequest req = reqOpt.get();
        if (req.getStatus() != 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu này đã được xử lý trước đó!"));
        }

        req.setStatus(1); // APPROVED
        req.setResolvedAt(LocalDateTime.now());
        payoutRequestRepository.save(req);

        // Cập nhật lại nhật ký giao dịch điểm với trạng thái đã duyệt
        Optional<User> memberOpt = userRepository.findById(req.getUserId());
        if (memberOpt.isPresent()) {
            User member = memberOpt.get();
            pointTransactionRepository.save(new PointTransaction(
                    member.getId(),
                    0.0,
                    "Yêu cầu rút tiền nhuận bút qua " + req.getPayoutMethod() + " (" + req.getPayoutInfo() + ") đã được Admin phê duyệt thành công!"
            ));
        }

        return ResponseEntity.ok(Map.of("message", "Phê duyệt yêu cầu rút tiền thành công!"));
    }

    // 7. Admin: Từ chối rút tiền (hoàn lại điểm cho tác giả)
    @PostMapping("/admin/payout-requests/{id}/reject")
    public ResponseEntity<?> rejectPayoutRequest(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Optional<PayoutRequest> reqOpt = payoutRequestRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Yêu cầu rút tiền không tồn tại!"));
        }

        PayoutRequest req = reqOpt.get();
        if (req.getStatus() != 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu này đã được xử lý trước đó!"));
        }

        req.setStatus(2); // REJECTED
        req.setResolvedAt(LocalDateTime.now());
        payoutRequestRepository.save(req);

        // Hoàn lại điểm
        Optional<User> memberOpt = userRepository.findById(req.getUserId());
        if (memberOpt.isPresent()) {
            User member = memberOpt.get();
            member.setPoints(Math.round((member.getPoints() + req.getPoints()) * 10.0) / 10.0);
            userRepository.save(member);

            // Lưu nhật ký giao dịch điểm hoàn trả
            pointTransactionRepository.save(new PointTransaction(
                    member.getId(),
                    req.getPoints(),
                    "Hoàn trả điểm: Yêu cầu rút tiền mã số #" + req.getId() + " bị từ chối."
            ));
        }

        return ResponseEntity.ok(Map.of("message", "Đã từ chối yêu cầu rút tiền và hoàn lại điểm!"));
    }

    // 8. Admin: Thống kê doanh thu, rút tiền và ủng hộ
    @GetMapping("/admin/revenue")
    public ResponseEntity<?> getRevenueStats(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        // Doanh thu từ thanh toán nâng cấp VIP
        List<PaymentOrder> paidOrders = paymentOrderRepository.findAll().stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .collect(Collectors.toList());
        double totalVipSales = paidOrders.stream().mapToDouble(PaymentOrder::getAmount).sum();

        // Doanh thu ủng hộ (Donation)
        List<Donation> donations = donationRepository.findAll();
        double totalDonations = donations.stream().mapToDouble(Donation::getAmount).sum();

        // Tổng chi trả tiền nhuận bút đã duyệt thành công
        List<PayoutRequest> approvedPayouts = payoutRequestRepository.findAll().stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toList());
        double totalPayoutMoney = approvedPayouts.stream().mapToDouble(PayoutRequest::getAmountMoney).sum();

        return ResponseEntity.ok(Map.of(
                "totalVipSales", totalVipSales,
                "totalDonations", totalDonations,
                "totalPayoutMoney", totalPayoutMoney,
                "vipCount", paidOrders.size(),
                "donationCount", donations.size(),
                "payoutCount", approvedPayouts.size()
        ));
    }

    @GetMapping("/admin/revenue/details")
    public ResponseEntity<?> getRevenueDetails(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<User> userOpt = authenticateUser(authHeader);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Quyền truy cập bị từ chối!"));
        }

        Map<Long, String> userIdToUsername = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (u1, u2) -> u1));

        List<Map<String, Object>> paidOrdersMapped = paymentOrderRepository.findAll().stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("userId", o.getUserId());
                    m.put("username", userIdToUsername.getOrDefault(o.getUserId(), "Unknown"));
                    m.put("amount", o.getAmount());
                    m.put("description", o.getDescription());
                    m.put("createdAt", o.getCreatedAt());
                    return m;
                })
                .sorted((a, b) -> ((Date) b.get("createdAt")).compareTo((Date) a.get("createdAt")))
                .collect(Collectors.toList());

        List<Map<String, Object>> donationsMapped = donationRepository.findAll().stream()
                .map(d -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", d.getId());
                    m.put("donorId", d.getDonorId());
                    m.put("donorName", d.getDonorName() != null ? d.getDonorName() : "Độc giả ẩn danh");
                    m.put("receiverId", d.getReceiverId());
                    m.put("receiverName", d.getReceiverId() != null ? userIdToUsername.getOrDefault(d.getReceiverId(), "Unknown") : "Tòa soạn");
                    m.put("amount", d.getAmount());
                    m.put("message", d.getMessage());
                    m.put("createdAt", d.getCreatedAt());
                    return m;
                })
                .sorted((a, b) -> ((Date) b.get("createdAt")).compareTo((Date) a.get("createdAt")))
                .collect(Collectors.toList());

        List<Map<String, Object>> payoutsMapped = payoutRequestRepository.findAll().stream()
                .filter(p -> p.getStatus() == 1)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("userId", p.getUserId());
                    m.put("username", userIdToUsername.getOrDefault(p.getUserId(), "Unknown"));
                    m.put("points", p.getPoints());
                    m.put("amountMoney", p.getAmountMoney());
                    m.put("payoutMethod", p.getPayoutMethod());
                    m.put("payoutInfo", p.getPayoutInfo());
                    m.put("createdAt", p.getCreatedAt());
                    m.put("resolvedAt", p.getResolvedAt());
                    return m;
                })
                .sorted((a, b) -> ((Date) b.get("createdAt")).compareTo((Date) a.get("createdAt")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "vipOrders", paidOrdersMapped,
                "donations", donationsMapped,
                "payouts", payoutsMapped
        ));
    }

    // 9. Public: Gửi ủng hộ (website hoặc tác giả cụ thể) và quy đổi sang điểm cho tác giả
    @PostMapping("/donations")
    public ResponseEntity<?> createDonation(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        
        Optional<User> donorOpt = authenticateUser(authHeader);
        Long donorId = donorOpt.map(User::getId).orElse(null);
        String donorName = donorOpt.map(User::getUsername).orElse((String) body.getOrDefault("donorName", "Độc giả ẩn danh"));

        double amount;
        try {
            amount = Double.parseDouble(body.get("amount").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số tiền ủng hộ không hợp lệ!"));
        }

        if (amount <= 0.0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số tiền ủng hộ phải lớn hơn $0.00!"));
        }

        Long receiverId = null;
        if (body.get("receiverId") != null && !body.get("receiverId").toString().isEmpty()) {
            try {
                receiverId = Long.parseLong(body.get("receiverId").toString());
            } catch (Exception e) {
                // receiverId không hợp lệ -> coi như ủng hộ website
            }
        }

        String message = (String) body.get("message");

        // Lưu đơn ủng hộ
        Donation donation = new Donation(donorId, donorName, receiverId, amount, message);
        Donation saved = donationRepository.save(donation);

        // Quy đổi điểm nhuận bút thưởng cho tác giả: $1.00 = 10 điểm
        if (receiverId != null) {
            Optional<User> receiverOpt = userRepository.findById(receiverId);
            if (receiverOpt.isPresent()) {
                User receiver = receiverOpt.get();
                double bonusPoints = amount * 10.0;
                receiver.setPoints(Math.round((receiver.getPoints() + bonusPoints) * 10.0) / 10.0);
                userRepository.save(receiver);

                // Ghi nhật ký cộng điểm nhuận bút cho tác giả
                pointTransactionRepository.save(new PointTransaction(
                        receiver.getId(),
                        bonusPoints,
                        "Nhận điểm thưởng ủng hộ từ độc giả @" + donorName + ". Lời nhắn: " + (message != null ? message : "Không có")
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Ủng hộ thành công! Cảm ơn sự đóng góp quý báu của bạn.",
                "donationId", saved.getId()
        ));
    }
}
