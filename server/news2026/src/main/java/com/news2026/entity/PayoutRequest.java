package com.news2026.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout_request")
public class PayoutRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private double points;

    @Column(name = "amount_money", nullable = false)
    private double amountMoney;

    @Column(name = "payout_method", nullable = false)
    private String payoutMethod; // "MOMO", "BANK"

    @Column(name = "payout_info", nullable = false, length = 500)
    private String payoutInfo;

    @Column(nullable = false)
    private int status = 0; // 0: PENDING, 1: APPROVED, 2: REJECTED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public PayoutRequest() {}

    public PayoutRequest(Long userId, double points, double amountMoney, String payoutMethod, String payoutInfo) {
        this.userId = userId;
        this.points = points;
        this.amountMoney = amountMoney;
        this.payoutMethod = payoutMethod;
        this.payoutInfo = payoutInfo;
        this.status = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public double getAmountMoney() { return amountMoney; }
    public void setAmountMoney(double amountMoney) { this.amountMoney = amountMoney; }
    public String getPayoutMethod() { return payoutMethod; }
    public void setPayoutMethod(String payoutMethod) { this.payoutMethod = payoutMethod; }
    public String getPayoutInfo() { return payoutInfo; }
    public void setPayoutInfo(String payoutInfo) { this.payoutInfo = payoutInfo; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
