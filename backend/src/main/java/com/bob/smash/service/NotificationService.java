package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.dto.NotificationMappingDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Notification;
import com.bob.smash.entity.NotificationMapping;

public interface NotificationService {
  // (등록) 알림 등록+매핑 동시
  NotificationDTO createNotification(NotificationDTO dto);
  // (목록) 회원별 알림 목록 조회
  List<NotificationMappingDTO> getNotificationByMember(String memberId);
  // (목록) 회원별 알림 목록 조회: 읽음 상태별
  List<NotificationMappingDTO> getUnreadNotifications(String memberId, boolean isRead);
  // (목록) 회원별 알림 목록 조회: 타입별
  List<NotificationMappingDTO> getNotificationsByType(String memberId, String targetType);
  // (개수) 회원별 읽지 않은 알림 개수
  int countUnreadNotifications(String memberId);
  // (읽음 처리)
  NotificationMappingDTO readNotification(String memberId, Integer idx);
  // (읽음 처리) 회원별 모든 알림 읽음 처리
  void markAllAsRead(String memberId);
  // (삭제) 회원별 특정 알림 삭제(매핑만 삭제)
  void deleteNotification(String memberId, Integer idx);
  // (삭제) 회원별 알림 삭제(매핑만 삭제): 회원 탈퇴용
  void deleteNotificationByMember(String memberId);
  // (삭제) 알림 일부 정리(매핑이 없는 경우)
  void deleteOrphanedNotifications();

  // dto → entity(Notification)
  default Notification dtoToEntity(NotificationDTO dto) {
    return Notification.builder()
                       .idx(dto.getIdx())
                       .notice(dto.getNotice())
                       .createdAt(dto.getCreatedAt())
                       .targetType(Notification.TargetType.valueOf(dto.getTargetType().toLowerCase()))
                       .targetIdx(dto.getTargetIdx())
                       .build();
  }
  // dto → entity(NotificationMapping)
  default NotificationMapping dtoToEntity(NotificationMappingDTO dto) {
    return NotificationMapping.builder()
                              .notification(dtoToEntity(dto.getNotification()))
                              .member(Member.builder().emailId(dto.getMemberId()).build())
                              .isRead(Boolean.TRUE.equals(dto.getIsRead()) ? (byte) 1 : (byte) 0)
                              .readAt(dto.getReadAt())
                              .build();
  }
  // entity → dto(Notification)
  default NotificationDTO entityToDto(Notification entity) {
    return NotificationDTO.builder()
                          .idx(entity.getIdx())
                          .notice(entity.getNotice())
                          .createdAt(entity.getCreatedAt())
                          .targetType(entity.getTargetType().name().toLowerCase())
                          .targetIdx(entity.getTargetIdx())
                          .build();
  }
  // entity → dto(NotificationMapping)
  default NotificationMappingDTO entityToDto(NotificationMapping entity) {
    return NotificationMappingDTO.builder()
                                 .notification(entityToDto(entity.getNotification()))
                                 .memberId(entity.getMember().getEmailId())
                                 .isRead(entity.getIsRead() == 1)
                                 .readAt(entity.getReadAt())
                                 .build();
  }
}