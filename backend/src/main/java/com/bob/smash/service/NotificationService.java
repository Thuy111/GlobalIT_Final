package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Notification;
import com.bob.smash.entity.NotificationMapping;

public interface NotificationService {
  // (등록) 알림 등록+매핑 동시
  NotificationDTO createNotification(NotificationDTO dto);
  // (목록) 회원별 알림 목록 조회
  List<NotificationDTO> getNotificationByMember(String memberId);
  // (목록) 회원별 알림 목록 조회: 읽음 상태별
  List<NotificationDTO> getUnreadNotifications(String memberId, boolean isRead);
  // (목록) 회원별 알림 목록 조회: 타입별
  List<NotificationDTO> getNotificationsByType(String memberId, String targetType);
  // (읽음 처리)
  NotificationDTO readNotification(String memberId, Integer idx);
  // (삭제) 회원별 특정 알림 삭제(매핑만 삭제)
  void deleteNotification(String memberId, Integer idx);
  // (삭제) 회원별 알림 삭제(매핑만 삭제)
  void deleteNotificationByMember(String memberId);
  // (삭제) 알림 일부 정리(매핑이 없는 경우)
  void deleteOrphanedNotifications();

  // dto → entity(Notification)
  default Notification dtoToEntity(NotificationDTO dto) {
    return Notification.builder()
                       .idx(dto.getIdx())
                       .notice(dto.getNotice())
                       .createdAt(dto.getCreatedAt())
                       .targetType(Notification.TargetType.valueOf(dto.getTargetType()))
                       .targetIdx(dto.getTargetIdx())
                       .build();
  }
  // dto → entity(NotificationMapping)
  default NotificationMapping dtoToMappingEntity(NotificationDTO dto) {
    return NotificationMapping.builder()
                              .notification(dtoToEntity(dto))
                              .member(Member.builder().emailId(dto.getMemberId()).build())
                              .isRead((byte) (dto.getIsRead() ? 1 : 0)) // 0: unread, 1: read
                              .readAt(dto.getIsRead() ? dto.getCreatedAt() : null) // 읽은 시간
                              .build();
  }
  // entity → dto
  default NotificationDTO entityToDto(Notification notification, NotificationMapping mapping) {
    return NotificationDTO.builder()
                          .idx(notification.getIdx())
                          .notice(notification.getNotice())
                          .createdAt(notification.getCreatedAt())
                          .isRead(mapping.getIsRead() == 1) // 0: unread, 1: read
                          .readAt(null != mapping.getReadAt() ? mapping.getReadAt() : null) // 읽은 시간
                          .targetType(notification.getTargetType().name().toLowerCase()) // request, estimate, review
                          .targetIdx(notification.getTargetIdx())
                          .memberId(mapping.getMember().getEmailId())
                          .build();
  }
}