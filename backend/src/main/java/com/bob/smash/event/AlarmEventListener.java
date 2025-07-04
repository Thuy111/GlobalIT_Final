package com.bob.smash.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    // 이벤트 발생한 의뢰서 정보 조회
    Request request = requestRepository.findById(event.getRequestIdx())
                                       .orElseThrow(() -> new RuntimeException("Request not found"));
    // 알림 수신 회원 설정 및 메시지 생성
    List<String> receiverId = new ArrayList<>();
    String message = String.format("의뢰서: %s", event.getAction().getDisplayName());
    if(event.getAction() == RequestEvent.Action.BID) {
      // 1. 낙찰 성공 견적서 조회 (isSelected = 2)
      List<Estimate> winEstimates = estimateRepository.findByRequest_IdxAndIsSelected(request.getIdx(), (byte)2);
      // 2. 낙찰 실패 견적서 조회 (isSelected = 1)
      List<Estimate> loseEstimates = estimateRepository.findByRequest_IdxAndIsSelected(request.getIdx(), (byte)1);
      // 3. 낙찰 성공 알림 발송
      for (Estimate win : winEstimates) {
        receiverId = List.of(win.getPartnerInfo().getMember().getEmailId());
        message = String.format(
          "[%s] 의뢰에 올린 견적서가 **낙찰되었습니다!** (금액: %,d원)",
          request.getTitle(), win.getPrice()
        );
        if(!receiverId.isEmpty()) {
          NotificationDTO dto = NotificationDTO.builder()
                                               .notice(message)
                                               .createdAt(LocalDateTime.now())
                                               .targetType("estimate")
                                               .targetIdx(win.getIdx())
                                               .memberIdList(List.of(win.getPartnerInfo().getMember().getEmailId()))
                                               .build();
          service.createNotification(dto);
        }
      }
      // 4. 낙찰 실패 알림 발송
      for (Estimate lose : loseEstimates) {
        receiverId = List.of(lose.getPartnerInfo().getMember().getEmailId());
        message = String.format(
            "[%s] 의뢰에 올린 견적서가 미선정(낙찰 실패)되었습니다.",
            request.getTitle()
        );
        if(!receiverId.isEmpty()) {
          NotificationDTO dto = NotificationDTO.builder()
                                               .notice(message)
                                               .createdAt(LocalDateTime.now())
                                               .targetType("request")
                                               .targetIdx(lose.getIdx())
                                               .memberIdList(List.of(lose.getPartnerInfo().getMember().getEmailId()))
                                               .build();
          service.createNotification(dto);
        }
      }
    } else {
      if(event.getAction() == RequestEvent.Action.UPDATED) {
        // 해당 의뢰서의 모든 견적서 작성자 ID를 검색
        List<Estimate> estimates = estimateRepository.findByRequest_Idx(event.getRequestIdx());
        List<String> partnerIdList = estimates.stream()
                                              .filter(e -> e.getPartnerInfo() != null && e.getPartnerInfo().getMember() != null)
                                              .map(e -> e.getPartnerInfo().getMember().getEmailId())
                                              .distinct() // 중복 제거 (동일 업체가 여러 번 견적을 썼을 수 있으므로)
                                              .toList();
        receiverId.addAll(partnerIdList); // 해당 의뢰서의 모든 견적서 작성자 ID를 수신자로 설정
        message = String.format(
          "%s님이 [%s] 의뢰의 내용을 수정했습니다. (사용일: %s, 지역: %s)",
          request.getMember().getNickname(), // 의뢰서 작성자 닉네임
          request.getTitle(), // 의뢰서 제목
          request.getUseDate().format(DateTimeFormatter.ofPattern("yy년 MM월 dd일")),
          request.getUseRegion()
        );
      } else if(event.getAction() == RequestEvent.Action.GET) {
        // 해당 의뢰서의 낙찰된 견적서 조회
        List<Estimate> estimates = estimateRepository.findByRequest_IdxAndIsSelected(event.getRequestIdx(), (byte)2);
        if (estimates.isEmpty()) {
          throw new RuntimeException("낙찰된 견적서가 없습니다.");
        } else {
          for (Estimate estimate : estimates) {
            // 낙찰된 견적서 작성자 ID를 수신자로 설정
            receiverId.add(estimate.getPartnerInfo().getMember().getEmailId());
          }
        }
        message = String.format(
          "%s님이 [%s] 의뢰 물품을 수령했습니다.",
          request.getMember().getNickname(), // 의뢰서 작성자 닉네임
          request.getTitle() // 의뢰서 제목
        );
      } else {
        throw new IllegalArgumentException("Unknown action type: " + event.getAction());
      }
      NotificationDTO dto = NotificationDTO.builder()
                                           .notice(message)
                                           .createdAt(LocalDateTime.now())
                                           .targetType("request")
                                           .targetIdx(event.getRequestIdx())
                                           .memberIdList(receiverId)
                                           .build();
      service.createNotification(dto);
    }
  }

  @EventListener
  public void handleEstimate(EstimateEvent event) {
    // 이벤트 발생한 견적서와 의뢰서 정보 조회
    Estimate estimate = estimateRepository.findById(event.getEstimateIdx())
                                          .orElseThrow(() -> new RuntimeException("Estimate not found"));
    Request request = requestRepository.findById(event.getRequestIdx())
                                       .orElseThrow(() -> new RuntimeException("Request not found"));
    // 알림 수신 회원 설정 및 메시지 생성
    List<String> receiverId = new ArrayList<>();
    String message = String.format("견적서: %s", event.getAction().getDisplayName());
    if(event.getAction() == EstimateEvent.Action.RETURNED) {
      // 견적서 반납의 경우
      receiverId.add(request.getMember().getEmailId()); // 의뢰서 작성자 ID를 수신자로 설정
      message = String.format(
        "%s에서 대여 물품 반납을 확인했습니다.",
        estimate.getPartnerInfo().getName() // 업체명
      );
    } else if(event.getAction() == EstimateEvent.Action.SELECTED) {
      // 견적서 자동 미낙찰의 경우
      receiverId.add(estimate.getPartnerInfo().getMember().getEmailId()); // 견적서 작성자 ID를 수신자로 설정
      message = String.format(
        "[%s] 의뢰에 올린 견적서가 미선정(낙찰 실패)되었습니다.", 
        request.getTitle() // 의뢰서 제목
      );
    } else {
      // 해당 의뢰서의 모든 견적서 작성자 ID를 검색
      List<Estimate> estimates = estimateRepository.findByRequest_Idx(event.getRequestIdx());
      List<String> partnerIdList = estimates.stream()
                                            .filter(e -> e.getPartnerInfo() != null && e.getPartnerInfo().getMember() != null)
                                            .map(e -> e.getPartnerInfo().getMember().getEmailId())
                                            .distinct() // 중복 제거 (동일 업체가 여러 번 견적을 썼을 수 있으므로)
                                            .toList();
      receiverId.addAll(partnerIdList); // 해당 의뢰서의 모든 견적서 작성자 ID를 수신자로 설정
      receiverId.add(request.getMember().getEmailId()); // 의뢰서 작성자 ID도 수신자로 추가
      // 해당 견적서 작성자는 제외
      final String excludeEmailId;
      if (estimate.getPartnerInfo() != null && estimate.getPartnerInfo().getMember() != null) {
        excludeEmailId = estimate.getPartnerInfo().getMember().getEmailId();
      } else {
        excludeEmailId = null;
      }
      if (excludeEmailId != null) {
        receiverId.removeIf(id -> id.equals(excludeEmailId));
      }
      if(event.getAction() == EstimateEvent.Action.CREATED) {
        // 견적서 신규 작성의 경우
        message = String.format(
          "%s에서 [%s] 의뢰에 새로운 견적서를 등록했습니다. (금액: %,d원)",
          estimate.getPartnerInfo().getName(), // 업체명
          request.getTitle(), // 의뢰서 제목
          estimate.getPrice()
        );
      } else if(event.getAction() == EstimateEvent.Action.UPDATED) {
        // 견적서 수정의 경우
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
    // 신규 알림 생성
    if(!receiverId.isEmpty()) {
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

  @EventListener
  public void handleReview(ReviewEvent event) {
    // 이벤트 발생한 리뷰 정보 조회
    Review review = reviewRepository.findById(event.getReviewIdx())
                                    .orElseThrow(() -> new RuntimeException("Review not found"));
    // 알림 수신 회원 설정 및 메시지 생성
    List<String> receiverId = new ArrayList<>();
    receiverId.add(review.getEstimate().getPartnerInfo().getMember().getEmailId()); // 견적서 작성자 ID를 수신자로 설정
    String message = String.format("리뷰: %s", event.getAction().getDisplayName());
    message = String.format(
          "%s님이 [%s] 의뢰의 견적서에 리뷰를 %s했습니다. (별점: %d점)",
          review.getMember().getNickname(), // 리뷰 작성자 닉네임
          review.getEstimate().getRequest().getTitle(),
          event.getAction().getDisplayName(), // 리뷰 작성 또는 수정
          review.getStar() // 리뷰 별점
    );
    // 신규 알림 생성
    if(!receiverId.isEmpty()) {
      NotificationDTO dto = NotificationDTO.builder()
                                           .notice(message)
                                           .createdAt(LocalDateTime.now())
                                           .targetType("review")
                                           .targetIdx(event.getReviewIdx())
                                           .memberIdList(receiverId)         
                                           .build();
      service.createNotification(dto);
    }
  }
}