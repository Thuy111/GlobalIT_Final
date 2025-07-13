package com.bob.smash.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.smash.entity.Notification;
import com.bob.smash.entity.NotificationMapping;

public interface NotificationMappingRepository extends JpaRepository<NotificationMapping, NotificationMapping.PK> {
  // 특정 회원의 알림 목록 조회
  @EntityGraph(attributePaths = {"notification"})
  List<NotificationMapping> findByMember_EmailIdOrderByNotification_CreatedAtDesc(String emailId);
  // (필터링용)특정 회원의 읽음 여부에 따른 알림 목록 조회
  @EntityGraph(attributePaths = {"notification"})
  List<NotificationMapping> findByMember_EmailIdAndIsReadOrderByNotification_CreatedAtDesc(String emailId, Byte isRead);
  // (필터링용)특정 회원의 타입에 따른 알림 목록 조회
  @EntityGraph(attributePaths = {"notification"})
  List<NotificationMapping> findByMember_EmailIdAndNotification_TargetTypeOrderByNotification_CreatedAtDesc(
    String emailId, Notification.TargetType targetType
  );
  // (개수 확인)특정 회원의 읽음 여부에 따른 알림 개수 조회
  int countByMember_EmailIdAndIsRead(String emailId, Byte isRead);
  // (읽음 처리, 삭제용)매핑 단건 조회
  NotificationMapping findByMember_EmailIdAndNotification_Idx(String emailId, Integer idx);
  // (탈퇴용)특정 회원의 모든 알림 삭제
  void deleteByMember_EmailId(String emailId);
}