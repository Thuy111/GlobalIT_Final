package com.bob.smash.controller;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.PaymentDTO;
import com.bob.smash.dto.RequestDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.service.EstimateService;
import com.bob.smash.service.RequestService;
import com.bob.smash.service.ReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/smash/request")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService requestService;
    private final EstimateService estimateService;
    private final ReviewService reviewService;

    @GetMapping("/")
    public String request() {
        return "redirect:/smash/request/list";
    }

    // 의뢰서 목록
    @GetMapping("/list")
    public void list(Model model) {
        model.addAttribute("title", "의뢰서 목록");
    }
    // 내가 쓴 의뢰서 목록
    @GetMapping("/mylist")
    public String myList(HttpSession session, RedirectAttributes redirectAttributes) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if(currentUser == null || currentUser.getRole() != 0) {
            // 일반 회원이 아닌 경우, 홈으로
            return "redirect:/smash";
        } else {
            // 일반 회원인 경우, 자신이 작성한 의뢰서 목록을 조회
            redirectAttributes.addFlashAttribute("result", requestService.getListByMemberId(currentUser.getEmailId()));
        }
        return "redirect:/smash/request/list";
    }
    // 전체 의뢰서 목록
    @GetMapping("/all")
    public String allList(HttpSession session, RedirectAttributes redirectAttributes) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        // currentUser가 null이면 홈으로 리다이렉트
        if (currentUser == null) {
            return "redirect:/smash";
        } else {
            if(currentUser.getRole() == 2) {
                // 관리자인 경우, 전체 의뢰서 목록을 조회
                redirectAttributes.addFlashAttribute("result", requestService.getList());
            } else {
                // 관리자가 아닌 경우, 자신이 작성한 의뢰서 목록을 조회
                return "redirect:/smash/request/mylist";
            }
        }
        return "redirect:/smash/request/list";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "의뢰서 작성");
        return "/smash/request/register";
    }
    @PostMapping("/register")  
    public String register(@ModelAttribute RequestDTO requestDTO,
                           @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                           Model model) {

        String mainAddress = requestDTO.getUseRegion() != null ? requestDTO.getUseRegion().trim() : "";
        String detailAddr = requestDTO.getDetailAddress() != null ? requestDTO.getDetailAddress().trim() : "";

        String fullAddress = mainAddress;
        if (!detailAddr.isEmpty()) {
            fullAddress += " " + detailAddr;
        }
        requestDTO.setUseRegion(fullAddress);
        requestDTO.setDetailAddress(null);

        Integer savedIdx = requestService.register(requestDTO, imageFiles);
        model.addAttribute("msg", savedIdx);
        return "redirect:/smash/request/detail/" + savedIdx;
    }

    // 수정: 의뢰서 상세 보기
    @GetMapping("/detail/{idx}")
    public String detail(@PathVariable("idx") Integer idx, Model model, OAuth2AuthenticationToken authentication) {
        RequestDTO dto = requestService.get(idx);
        model.addAttribute("dto", dto);
        model.addAttribute("title", dto.getTitle());

        String currentUserEmail = (authentication != null) ? authentication.getPrincipal().getAttribute("email") : null;
        model.addAttribute("currentUserEmail", currentUserEmail);

        List<EstimateDTO> estimates = estimateService.getListByRequestIdx(idx);
        model.addAttribute("estimates", estimates);

        // 견적서 ID별 리뷰 리스트 Map 추가
        Map<Integer, List<ReviewDTO>> estimateReviewMap = new HashMap<>();
        for (EstimateDTO estimate : estimates) {
          List<ReviewDTO> reviews = reviewService.getReviewsByEstimateIdx(estimate.getIdx());
            estimateReviewMap.put(estimate.getIdx(), reviews);
        }
        model.addAttribute("estimateReviewMap", estimateReviewMap); // 모델에 추가

        // 유저가 작성한 리뷰 여부 (기존 코드 유지)
        Map<Integer, Boolean> reviewStatusMap = new HashMap<>();
        if (currentUserEmail != null) {
            for (EstimateDTO estimate : estimates) {
                boolean reviewed = reviewService.hasUserReviewed(currentUserEmail, estimate.getIdx());
                reviewStatusMap.put(estimate.getIdx(), reviewed);
            }
        }
        model.addAttribute("reviewStatusMap", reviewStatusMap);

        return "smash/request/detail";
    }

    @PostMapping("/delete")
    public String deleteRequest(@RequestParam("idx") Integer idx) {
        requestService.delete(idx);
        return "redirect:/smash/";
    }

    @GetMapping("/update/{idx}")
    public String modifyForm(@PathVariable("idx") Integer idx, Model model) {
        RequestDTO dto = requestService.get(idx);
        model.addAttribute("title", "의뢰서 수정");
        model.addAttribute("dto", dto);
        return "/smash/request/update"; 
    }

    @PostMapping("/modify")
    public String modifyRequest(@ModelAttribute RequestDTO dto,
                                @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,
                                @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {
        
        String mainAddress = dto.getUseRegion() != null ? dto.getUseRegion().trim() : "";
        String detailAddr = dto.getDetailAddress() != null ? dto.getDetailAddress().trim() : "";

        String fullAddress = mainAddress;
        if (!detailAddr.isEmpty()) {
            fullAddress += " " + detailAddr;
        }
        dto.setUseRegion(fullAddress);   
        dto.setDetailAddress(null); 
        
        requestService.modify(dto, newImages, deleteImageIds);
        return "redirect:/smash/request/detail/" + dto.getIdx();
    }

    @PostMapping("/changeIsDone")
    public ResponseEntity<?> changeIsDone(@RequestParam("requestIdx") Integer idx,
                                          @RequestParam("estimateIdx") Integer eIdx,
                                          @RequestBody PaymentDTO dto) {
        try {
            Integer paymentIdx = requestService.changeIsDone(idx, 
                                                             eIdx, 
                                                             dto.getMemberEmail(), 
                                                             dto.getPartnerBno(), 
                                                             dto.getSuggestedPrice());
            Map<String, Object> response = new HashMap<>();

            response.put("message", "성공적으로 낙찰 되었습니다. 결제서로 이동합니다.");
            response.put("paymentIdx", paymentIdx);

            return ResponseEntity.ok(response);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("낙찰 처리 중 오류 발생");
        }
    }

    @PostMapping("/changeIsGet")
    public String confirmGet(@RequestParam("requestIdx") Integer requestIdx,
                             RedirectAttributes rttr) {
        requestService.changeIsGet(requestIdx);
        rttr.addFlashAttribute("message", "대여 확인하셨습니다");
        return "redirect:/smash/request/detail/" + requestIdx;
    }

    // 홈페이지용 목록 조회
    @GetMapping("/main")
    public ResponseEntity<Map<String, Object>> getPagedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(requestService.getPagedRequestList(page, size, search));
    }
}