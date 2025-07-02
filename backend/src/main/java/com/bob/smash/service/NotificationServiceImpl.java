package com.bob.smash.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.dto.NotificationMappingDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Notification;
import com.bob.smash.entity.NotificationMapping;
import com.bob.smash.repository.NotificationMappingRepository;
import com.bob.smash.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository repository;
  private final NotificationMappingRepository mappingRepository;

  // (등록)알림 등록+매핑 동시
  @Override
  @Transactional
  public NotificationDTO createNotification(NotificationDTO dto) {
    // 알림(Notification) 엔티티 생성 및 저장
    Notification notification = repository.save(dtoToEntity(dto));
    // 매핑(NotificationMapping) 리스트 생성 및 저장
    if (dto.getMemberIdList() != null) {
      for (String memberId : dto.getMemberIdList()) {
        NotificationMapping mapping = NotificationMapping.builder()
                                                         .notification(notification)
                                                         .member(Member.builder().emailId(memberId).build())
                                                         .isRead((byte) 0) // 생성시 무조건 미읽음
                                                         .readAt(null)
                                                         .build();
        mappingRepository.save(mapping);
      }
    }
    // 저장된 알림 정보를 DTO로 반환 (memberIdList는 그대로 반환)
    return entityToDto(notification);
  }

  // (목록)회원별 알림 목록 조회
  @Override
  public List<NotificationMappingDTO> getNotificationByMember(String memberId) {
    // 회원별 알림(NotificationMapping) 목록을 최신순으로 조회
    List<NotificationMapping> mappings = mappingRepository.findByMember_EmailIdOrderByNotification_CreatedAtDesc(memberId);
    // NotificationMapping → NotificationDTO 변환 (알림 내용만 리스트로 반환)
    return mappings.stream()
                   .map(this::entityToDto)
                   .toList();
  }
  // (목록)회원별 알림 목록 조회: 읽음 상태별
  @Override
  public List<NotificationMappingDTO> getUnreadNotifications(String memberId, boolean isRead) {
    byte readByte = isRead ? (byte) 1 : (byte) 0;
    List<NotificationMapping> mappings = mappingRepository.findByMember_EmailIdAndIsReadOrderByNotification_CreatedAtDesc(memberId, readByte);
    return mappings.stream()
                   .map(this::entityToDto)
                   .toList();
  }
  // (목록)회원별 알림 목록 조회: 타입별
  @Override
  public List<NotificationMappingDTO> getNotificationsByType(String memberId, String targetType) {
    Notification.TargetType typeEnum = Notification.TargetType.valueOf(targetType.toLowerCase());
    List<NotificationMapping> mappings = mappingRepository.findByMember_EmailIdAndNotification_TargetTypeOrderByNotification_CreatedAtDesc(memberId, typeEnum);
    return mappings.stream()
                   .map(this::entityToDto)
                   .toList();
  }
  // (개수) 회원별 읽은/읽지 않은 알림 개수 조회
  @Override
  public int countUnreadNotifications(String memberId, boolean isRead) {
    byte readByte = isRead ? (byte) 1 : (byte) 0;
    return mappingRepository.countByMember_EmailIdAndIsRead(memberId, readByte);
  }

  // (읽음 처리)
  @Override
  @Transactional
  public NotificationMappingDTO readNotification(String memberId, Integer idx) {
    // 회원-알림 매핑(NotificationMapping) 엔티티를 조회 (memberId, 알림 idx로)
    NotificationMapping mapping = mappingRepository.findByMember_EmailIdAndNotification_Idx(memberId, idx);
    // 이미 읽음이 아니면 읽음 처리
    if (mapping.getIsRead() == 0) {
      mapping.changeIsRead((byte)1);;
      mappingRepository.save(mapping);
    }
    // DTO로 변환해서 반환
    return entityToDto(mapping);
  }
  // (읽음 처리)회원별 모든 알림 읽음 처리
  @Override
  @Transactional
  public void markAllAsRead(String memberId) {
    // 해당 회원의 모든 미읽음 알림 매핑 조회
    List<NotificationMapping> unreadMappings = mappingRepository.findByMember_EmailIdAndIsReadOrderByNotification_CreatedAtDesc(memberId, (byte) 0);
    for (NotificationMapping mapping : unreadMappings) {
        mapping.changeIsRead((byte) 1); // 읽음 처리(커스텀 setter)
    }
  }

  // (삭제)회원별 특정 알림 삭제(매핑만 삭제)
  @Override
  @Transactional
  public void deleteNotification(String memberId, Integer idx) {
    // 매핑 단건 조회
    NotificationMapping mapping = mappingRepository.findByMember_EmailIdAndNotification_Idx(memberId, idx);
    if (mapping != null) {
      mappingRepository.delete(mapping);
    } else {
      // 필요시 예외 처리(존재하지 않는 경우)
      throw new IllegalArgumentException("알림이 존재하지 않습니다.");
    }
  }
  // (삭제)회원별 알림 삭제(매핑만 삭제): 회원 탈퇴용
  @Override
  @Transactional
  public void deleteNotificationByMember(String memberId) {
    mappingRepository.deleteByMember_EmailId(memberId);
  }
  // (삭제-스케줄)알림 정리(매핑이 없는 경우)
  @Override
  @Transactional
  @Scheduled(cron = "0 0 * * * ?") // 매시간 정각에 실행
  //@Sheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
  public void deleteOrphanedNotifications() {
    // 모든 알림을 가져와서, 매핑이 없는 알림을 찾아 삭제
    List<Notification> orphaned = repository.findOrphanedNotifications();
    for (Notification notification : orphaned) {
      repository.delete(notification);
    }
  }
}