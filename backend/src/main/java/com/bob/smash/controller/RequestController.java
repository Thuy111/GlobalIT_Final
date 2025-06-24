package com.bob.smash.controller;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.service.EstimateService;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/smash/request")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService requestService;
    private final EstimateService estimateService;
    private final MemberRepository memberRepository; 

    @GetMapping("/")
    public String estimate() {
    return "redirect:/smash/request/listTest";
  }

    // 의뢰서 목록 보기/////////////////////////////////////  (test 용 추후 삭제 필요)
    @GetMapping("/listTest")
    public String list(Model model) {
        List<RequestDTO> result = requestService.getList();
        model.addAttribute("result", result); 
        return "smash/request/listTest";
    }

    // 의뢰서 작성 폼 보기////////////////////////////////////////////////////////////////////
    @GetMapping("/register")
    public String register() {
        return "/smash/request/register";
    }

    // 의뢰서 등록 처리
    @PostMapping("/register")
   
    public String register(@ModelAttribute RequestDTO requestDTO,                                              
                         @RequestParam("imageFiles") List<MultipartFile> imageFiles, //사진
                         @AuthenticationPrincipal OAuth2User oauth2User,
                         Model model) {

        String email;

        if (oauth2User.getAttribute("email") != null) { // 구글 계정
            email = (String) oauth2User.getAttribute("email");
        } else { // 카카오 계정
            Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
            email = (String) kakaoAccount.get("email");
        }
        log.info(" Logged in email: {}", email);

        Member member = memberRepository.findByEmailId(email)
                        .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다"));


        // 주소 조합 (메인 주소 + 상세 주소)
        String mainAddress = requestDTO.getUseRegion() != null ? requestDTO.getUseRegion().trim() : "";
        String detailAddr = requestDTO.getDetailAddress() != null ? requestDTO.getDetailAddress().trim() : "";

        String fullAddress = mainAddress;
        if (!detailAddr.isEmpty()) {
            fullAddress += " " + detailAddr;
        }

        requestDTO.setUseRegion(fullAddress);     // 전체 주소를 useRegion에 저장
        requestDTO.setDetailAddress(null);        // detailAddress는 DB에 저장하지 않음                 

        Integer savedIdx = requestService.register(requestDTO, member,imageFiles);
        model.addAttribute("msg", savedIdx);

        // return "redirect:/smash/request/listTest";
        return "redirect:/smash/request/detail/" + savedIdx;
    }

    //  의뢰서 상세 보기 ///////////////////////////////////////////////   
    @GetMapping("/detail/{idx}")
    public String detail(@PathVariable("idx") Integer idx, Model model) {
        RequestDTO dto = requestService.get(idx);
        model.addAttribute("dto", dto);
        // 해당 의뢰서에 대한 견적서 목록도 가져오기
        List<EstimateDTO> estimates = estimateService.getListByRequestIdx(idx);
        model.addAttribute("estimates", estimates);
        return "smash/request/detail";
    }

    // 의뢰서 삭제///////////////////////////////////////////////////
     @PostMapping("/delete")
      public String deleteRequest(@RequestParam("idx") Integer idx) {
        requestService.delete(idx);
        return "redirect:/smash/request/listTest";
    }


    // 수정////////////////////////////////////////////////////////////////////
    @GetMapping("/update/{idx}")
    public String modifyForm(@PathVariable("idx") Integer idx, Model model) {
        RequestDTO dto = requestService.get(idx);
        model.addAttribute("dto", dto);
        return "/smash/request/update"; 
    }

    @PostMapping("/modify")
    public String modifyRequest(
            @ModelAttribute RequestDTO dto,
            @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,  // 이미 업로드된 이미지 나올때 삭제할 수 있게
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) {
        
        //주소
        String mainAddress = dto.getUseRegion() != null ? dto.getUseRegion().trim() : "";
        String detailAddr = dto.getDetailAddress() != null ? dto.getDetailAddress().trim() : "";

        String fullAddress = mainAddress;
        if (!detailAddr.isEmpty()) {
            fullAddress += " " + detailAddr;
        }
        dto.setUseRegion(fullAddress);   
        dto.setDetailAddress(null); 
        
        requestService.modify(dto, newImages);        
        return "redirect:/smash/request/detail/" + dto.getIdx();
    }

    // ⭐ 추가
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPagedRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search){

        return ResponseEntity.ok(requestService.getPagedRequestList(page, size, search));
    }


}