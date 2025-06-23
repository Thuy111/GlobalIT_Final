package com.bob.smash.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.entity.Notification;
import com.bob.smash.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository repository;

  // 등록
  @Override
  public Integer register(NotificationDTO dto) {
    Notification notification = dtoToEntity(dto);
    repository.save(notification);
    return notification.getIdx();
  }

  // 읽지 않은 알림 개수 조회
  @Override
  public long countUnreadByMemberId(String memberId) {
    return repository.countByMember_EmailIdAndIsRead(memberId, (byte)0);
  }

  // 목록
  @Override
  public List<NotificationDTO> getList() {
    List<Notification> result = repository.findAll();
    return result.stream()
                 .map(notification -> entityToDto(notification))
                 .toList();
  }

  // 목록: 특정 회원의 알림 목록 조회
  @Override
  public NotificationDTO get(Integer idx) {
    Notification notification = repository.findById(idx)
                                          .orElseThrow(() -> new IllegalArgumentException("Notification not found with idx: " + idx));
    return entityToDto(notification);
  }

  // 읽음 처리
  @Override
  @Transactional
  public Integer markAsRead(Integer idx) {
    Notification notification = repository.findById(idx)
                                          .orElseThrow(() -> new IllegalArgumentException("Notification not found with idx: " + idx));
    notification.changeIsRead((byte) 1); // 1로 설정하여 읽음 처리
    return notification.getIdx();
  }

  // 삭제
  @Override
  public void delete(Integer idx) {
    Notification notification = repository.findById(idx)
                                          .orElseThrow(() -> new IllegalArgumentException("Notification not found with idx: " + idx));
    repository.delete(notification);
  }
  // 삭제: 회원탈퇴시 일괄 삭제
  @Override
  public void deleteByMemberId(String memberId) {
    List<Notification> notifications = repository.findByMember_EmailId(memberId);
    if (!notifications.isEmpty()) {
      repository.deleteAll(notifications);
    }
  }
}