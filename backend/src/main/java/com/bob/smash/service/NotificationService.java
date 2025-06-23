package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Notification;

public interface NotificationService {
  // 등록
  Integer register(NotificationDTO dto);
  // 읽지 않은 알림 개수 조회
  long countUnreadByMemberId(String memberId);
  // 목록
  List<NotificationDTO> getList();
  // 조회
  NotificationDTO get(Integer idx);
  // 읽음 처리
  Integer markAsRead(Integer idx);
  // 삭제
  void delete(Integer idx);
  // 삭제: 회원탈퇴시 일괄 삭제
  void deleteByMemberId(String memberId);

  // dto → entity
  default Notification dtoToEntity(NotificationDTO dto) {
    return Notification.builder()
                       .idx(dto.getIdx())
                       .member(Member.builder().emailId(dto.getMemberId()).build())
                       .notice(dto.getNotice())
                       .createdAt(dto.getCreatedAt())
                       .isRead(Boolean.TRUE.equals(dto.getIsRead()) ? (byte) 1 : (byte) 0)
                       .targetType(Notification.TargetType.valueOf(dto.getTargetType()))
                       .targetIdx(dto.getTargetIdx())
                       .build();
  }
  // entity → dto
  default NotificationDTO entityToDto(Notification entity) {
    return NotificationDTO.builder()
                          .idx(entity.getIdx())
                          .memberId(entity.getMember().getEmailId())
                          .notice(entity.getNotice())
                          .createdAt(entity.getCreatedAt())
                          .isRead(entity.getIsRead() == 1)
                          .targetType(entity.getTargetType().name())
                          .targetIdx(entity.getTargetIdx())
                          .build();
  }
}