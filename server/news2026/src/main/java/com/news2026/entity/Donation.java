package com.news2026.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "donation")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id")
    private Long donorId;

    @Column(name = "donor_name")
    private String donorName;

    @Column(name = "receiver_id")
    private Long receiverId; // null if donating to website

    @Column(nullable = false)
    private double amount;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private String status = "PAID";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    public Donation() {}

    public Donation(Long donorId, String donorName, Long receiverId, double amount, String message) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.receiverId = receiverId;
        this.amount = amount;
        this.message = message;
        this.status = "PAID";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
