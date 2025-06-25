package com.bob.smash.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bob.smash.dto.NotificationDTO;
import com.bob.smash.entity.Notification;
import com.bob.smash.repository.NotificationMappingRepository;
import com.bob.smash.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository repository;
  private final NotificationMappingRepository mappingRepository;
  @Override
  public NotificationDTO createNotification(NotificationDTO dto) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createNotification'");
  }
  @Override
  public List<NotificationDTO> getNotificationByMember(String memberId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getNotificationByMember'");
  }
  @Override
  public List<NotificationDTO> getUnreadNotifications(String memberId, boolean isRead) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getUnreadNotifications'");
  }
  @Override
  public List<NotificationDTO> getNotificationsByType(String memberId, String targetType) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getNotificationsByType'");
  }
  @Override
  public NotificationDTO readNotification(String memberId, Integer idx) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'readNotification'");
  }
  @Override
  public void deleteNotification(String memberId, Integer idx) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteNotification'");
  }
  @Override
  public void deleteNotificationByMember(String memberId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteNotificationByMember'");
  }
  @Override
  public void deleteOrphanedNotifications() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteOrphanedNotifications'");
  }
}