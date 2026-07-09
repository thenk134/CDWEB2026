package com.news2026.repository;

import com.news2026.entity.PayoutRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, Long> {
    List<PayoutRequest> findByUserIdOrderByIdDesc(Long userId);
    List<PayoutRequest> findAllByOrderByIdDesc();
}
