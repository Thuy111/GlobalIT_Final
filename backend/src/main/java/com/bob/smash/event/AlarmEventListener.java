package com.bob.smash.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.bob.smash.entity.Request;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Review;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.RequestRepository;
import com.bob.smash.repository.ReviewRepository;
import com.bob.smash.service.NotificationService;
import com.bob.smash.dto.NotificationDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlarmEventListener {
  private final NotificationService service;
  private final RequestRepository requestRepository;
  private final EstimateRepository estimateRepository;
  private final ReviewRepository reviewRepository;

  @EventListener
  public void handleRequest(RequestEvent event) {
  }

  @EventListener
  public void handleEstimate(EstimateEvent event) {
    Estimate estimate = estimateRepository.findById(event.getEstimateIdx())
                                          .orElseThrow(() -> new RuntimeException("Estimate not found"));
    Request request = requestRepository.findById(event.getRequestIdx())
                                       .orElseThrow(() -> new RuntimeException("Request not found"));
    // 알림 수신 회원 설정 및 메시지 생성
    List<String> receiverId = new ArrayList<>();
    String message;
    if(event.getAction() == EstimateEvent.Action.RETURNED) {
      receiverId.add(request.getMember().getEmailId()); // 의뢰서 작성자 ID를 수신자로 설정
      message = String.format(
        "%s에서 대여 물품 반납을 확인했습니다.",
        estimate.getPartnerInfo().getName() // 업체명
      );
    } else {
      // 견적서 생성/수정 알림 수신 회원 설정
      // 해당 의뢰서의 모든 견적서 작성자 ID를 검색
      List<Estimate> estimates = estimateRepository.findByRequest_Idx(event.getRequestIdx());
      List<String> partnerIdList = estimates.stream()
                                            .map(e -> e.getPartnerInfo().getMember().getEmailId())
                                            .distinct() // 중복 제거 (동일 업체가 여러 번 견적을 썼을 수 있으므로)
                                            .toList();
      receiverId.addAll(partnerIdList); // 견적서 작성자 ID를 수신자로 설정
      receiverId.add(request.getMember().getEmailId()); // 의뢰서 작성자 ID도 수신자로 추가
      receiverId.removeIf(id -> id.equals(estimate.getPartnerInfo().getMember().getEmailId())); // 신규 견적서 작성자 ID는 제외
      if(event.getAction() == EstimateEvent.Action.CREATED) {
        message = String.format(
          "%s에서 [%s] 의뢰에 새로운 견적서를 등록했습니다. (금액: %,d원)",
          estimate.getPartnerInfo().getName(), // 업체명
          request.getTitle(), // 의뢰서 제목
          estimate.getPrice()
        );
      } else if(event.getAction() == EstimateEvent.Action.UPDATED) {
        message = String.format(
          "%s에서 [%s] 의뢰의 견적서를 수정했습니다. (금액: %,d원)",
          estimate.getPartnerInfo().getName(), // 업체명
          request.getTitle(), // 의뢰서 제목
          estimate.getPrice()
        );
      } else {
        throw new IllegalArgumentException("Unknown action type: " + event.getAction());
      }
    }
    NotificationDTO dto = NotificationDTO.builder()
                                         .notice(message)
                                         .createdAt(LocalDateTime.now())
                                         .targetType("estimate")
                                         .targetIdx(event.getEstimateIdx())
                                         .memberIdList(receiverId)
                                         .build();
    service.createNotification(dto);
  }
}