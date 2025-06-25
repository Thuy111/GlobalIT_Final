package com.bob.smash.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.smash.entity.Notification;
import com.bob.smash.entity.NotificationMapping;

public interface NotificationMappingRepository extends JpaRepository<NotificationMapping, NotificationMapping.PK> {
  // 특정 회원의 알림 목록 조회
  List<NotificationMapping> findByMember_EmailIdOrderByNotification_CreatedAtDesc(String emailId);
  // 특정 회원의 읽음 여부에 따른 알림 목록 조회
  List<NotificationMapping> findByMember_EmailIdAndIsRead(String emailId, Byte isRead);
  // (개수 확인)특정 회원의 읽음 여부에 따른 알림 개수 조회
  long countByMember_EmailIdAndIsRead(String emailId, Byte isRead);
  // (필터링용)특정 회원의 알림 목록 조회(읽음 여부와 특정 시간 기준)
  List<NotificationMapping> findByMember_EmailIdAndNotification_CreatedAtAfterAndIsRead(
    String emailId, LocalDateTime after, Byte isRead
  );
  // (필터링용)특정 회원의 알림 목록 조회(카테고리 기준)
  List<NotificationMapping> findByMember_EmailIdAndNotification_TargetTypeOrderByNotification_CreatedAtDesc(
    String emailId, Notification.TargetType targetType
  );
  // 특정 회원의 모든 알림 삭제
  void deleteByMember_EmailId(String emailId);
}