package com.bob.smash.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.NotificationMappingDTO;
import com.bob.smash.service.NotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/alarm")
public class NotificationController {
  private final NotificationService service;

  @GetMapping({"","/"})
  public String alarm() {
    return "redirect:/smash/alarm/list";
  }
  
  //목록
  @GetMapping("/list")
  public ResponseEntity<List<NotificationMappingDTO>> list(HttpSession session, @RequestParam(required = false) String type) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    if (currentUser == null) {
      return ResponseEntity.ok(List.of());
    }
    List<NotificationMappingDTO> result;
    if (type == null) {
      result = service.getNotificationByMember(currentUser.getEmailId());
    } else {
      result = service.getNotificationsByType(currentUser.getEmailId(), type);
    }
    return ResponseEntity.ok(result);
  }
  // 미읽음 알림 개수
  @GetMapping("/unread")
  public ResponseEntity<Integer> unreadCount(HttpSession session) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    if (currentUser == null) {
      return ResponseEntity.ok(0);
    }
    // 현재 사용자의 읽지 않은 알림 개수 조회
    int count = service.countUnreadNotifications(currentUser.getEmailId(), false);
    return ResponseEntity.ok(count);
  }

  // 알림 읽음 처리
  @PostMapping("/read")
  public String alarmRead(@RequestParam("idx") Integer idx,
                          HttpSession session,
                          RedirectAttributes rttr) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    if (currentUser == null) {
      rttr.addFlashAttribute("error", "로그인이 필요합니다.");
      return "redirect:/smash";
    }
    service.readNotification(currentUser.getEmailId(), idx);
    return "ok";
  }

  // 전체 읽음 처리
  @PostMapping("/read/all")
  public String readAll(HttpSession session) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    if (currentUser == null) {
      return "로그인이 필요합니다.";
    }
    service.markAllAsRead(currentUser.getEmailId());
    return "ok";
  }

  // 알림 삭제 (단건)
  @PostMapping("/delete")
  public String delete(@RequestParam("idx") Integer idx, HttpSession session) {
    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    if (currentUser == null) {
      return "로그인이 필요합니다.";
    }
    service.deleteNotification(currentUser.getEmailId(), idx);
    return "ok";
  }
}