package com.bob.smash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bob.smash.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // 알림 조회: 회원 ID로 알림 목록 조회
    List<Notification> findByMemberId(String memberId);
    // 특정 회원의 읽지 않은 알림 개수 조회
    long countByMemberIdAndIsReadFalse(String memberId);
}