package com.bob.smash.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.dto.PartnerVerificationResponseDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.PaymentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerInfoServiceImpl implements PartnerInfoService {

  private final MemberRepository memberRepository;
  private final PaymentRepository paymentRepository;
  private final PartnerInfoRepository partnerInfoRepository;

  
  // 파트너 정보 조회 : 이메일
  @Override
  public PartnerInfoDTO getPartnerInfo(String emailId){
    PartnerInfo partnerInfo = partnerInfoRepository.findByMember_EmailId(emailId).orElse(null);
    if (partnerInfo == null) {
      return null; // 파트너 정보가 없으면 null 반환
    }
    return entityToDto(partnerInfo);
  }
  // 파트너 정보 조회 : 사업자 번호
  @Override
  public PartnerInfoDTO getPartnerInfoByBno(String bno) {
    PartnerInfo partnerInfo = partnerInfoRepository.findByBno(bno).orElse(null);
    if (partnerInfo == null) {
      return null; // 파트너 정보가 없으면 null 반환
    }
    return entityToDto(partnerInfo);
  }

  // 파트너 삭제
  @Override
  @Transactional
  public void deleteByMemberEmail(String email) {
    String bno = partnerInfoRepository.findBnoByMember_EmailId(email);// bno 조회
    paymentRepository.deleteByPartnerInfo_Bno(bno);
    partnerInfoRepository.deleteByMember_EmailId(email);
  }



  @Value("${openapi.business-status-key}") 
  private String businessStatusApiKey;

  // bno 입력 시 API를 이용해 검증
  @Override
  @Transactional
  public PartnerVerificationResponseDTO verifyAndRegister(String emailId, PartnerInfoDTO dto) {
    Member member = memberRepository.findByEmailId(emailId)
              .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

    // 해당 bno가 이미 다른 유저에게 등록되어 있는지 확인
    boolean bnoExists = partnerInfoRepository.findByBno(dto.getBno()).isPresent();
    if (bnoExists) {
      return new PartnerVerificationResponseDTO(false, "이미 등록된 사업자 번호입니다.");
    }


      // 사업자 인증을 공공데이터 API로 진행
    URI uri = UriComponentsBuilder
              .fromUriString("https://api.odcloud.kr/api/nts-businessman/v1/status")
              .queryParam("serviceKey", businessStatusApiKey)
              .queryParam("returnType", "JSON")
              .build(true)
              .toUri();

    Map<String, Object> body = Map.of("b_no", List.of(dto.getBno()));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);

    List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
    if (dataList == null || dataList.isEmpty()) {
      return new PartnerVerificationResponseDTO(false, "사업자 정보를 확인할 수 없습니다.");
    }

    String statusCode = (String) dataList.get(0).get("b_stt_cd");
    
    // 01 -> 계속 사업자, 02 -> 휴업자, 03 -> 폐업자
    if (!"01".equals(statusCode)) {
      return new PartnerVerificationResponseDTO(false, "유효하지 않은 사업자입니다.");
    }

    // 무작위 난수 생성하여 중복 검사
    String code;
    do {
      code = generateRandomCode();
    } while (partnerInfoRepository.existsByCode(code));

    // 파트너 정보 저장
    PartnerInfo partnerInfo = PartnerInfo.builder()
              .bno(dto.getBno())
              .member(member)
              .name(dto.getName())
              .tel(dto.getTel())
              .region(dto.getRegion())
              .description(dto.getDescription())
              .code(code)  // 생성된 무작위 난수 코드 저장
              .visitCnt(0)
              .build();

    partnerInfoRepository.save(partnerInfo);

    // 사업자 인증 완료 후 role을 1(사업자)로 변경
    member.changeRole((byte) 1);
    memberRepository.save(member);

    updateSecurityContextRole(member); // 즉시 권한 반영

    return new PartnerVerificationResponseDTO(true, "사업자 등록 및 전환 완료");
  }

  // 무작위 코드 생성 
  private String generateRandomCode() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 10);  // 10자리의 랜덤 코드
  }

  // 파트너 -> 유저
  @Override
  @Transactional
  public void convertToUser(String emailId) {
    Member member = memberRepository.findByEmailId(emailId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

    member.changeRole((byte)0);    
    updateSecurityContextRole(member); // 즉시 권한 반영
  }

  // 유저(사업자 번호가 DB에 등록된) -> 파트너
  @Override
  @Transactional
  public void updateRoleIfPartnerInfoExists(String emailId) {
    Member member = memberRepository.findByEmailId(emailId)
              .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

    boolean hasPartnerInfo = partnerInfoRepository.findByMember_EmailId(emailId).isPresent();
    if (hasPartnerInfo) {
      member.changeRole((byte)1);
      memberRepository.save(member);
      updateSecurityContextRole(member); // 즉시 권한 반영
    }
  }

  
  // 세션의 currentUser role 업데이트 헬퍼 메서드
  private void updateSessionRole(Byte newRole) {
    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attr == null) return;
    HttpServletRequest request = attr.getRequest();
    HttpSession session = request.getSession(false);
    if (session == null) return;

    CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
    if (currentUser == null) return;

    CurrentUserDTO updatedUser = CurrentUserDTO.builder()
        .emailId(currentUser.getEmailId())
        .nickname(currentUser.getNickname())
        .role(newRole)
        .bno(currentUser.getBno())
        .build();

    session.setAttribute("currentUser", updatedUser);
  }

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private HttpServletResponse response;

  @Autowired
  private OAuth2AuthorizedClientService authorizedClientService;

  @Autowired
  private OAuth2AuthorizedClientRepository authorizedClientRepository;

  private void updateSecurityContextRole(Member member) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      if (auth == null || !(auth instanceof OAuth2AuthenticationToken)) return;
      OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;

      OAuth2User currentOAuth2User = oauthToken.getPrincipal();
      Map<String, Object> attributes = new HashMap<>(currentOAuth2User.getAttributes());

      // ✅ Kakao 사용자라면 email 추출
      if (!attributes.containsKey("email") && attributes.containsKey("kakao_account")) {
          Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
          attributes.put("email", kakaoAccount.get("email"));
      }

      // ✅ 권한 설정
      List<GrantedAuthority> updatedAuthorities = List.of(
          new SimpleGrantedAuthority(member.getRole() == 1 ? "ROLE_PARTNER" : "ROLE_USER")
      );

      // ✅ 새로운 OAuth2User 생성
      OAuth2User updatedUser = new DefaultOAuth2User(
          updatedAuthorities,
          attributes,
          "email"
      );

      // ✅ registrationId 유지
      String registrationId = oauthToken.getAuthorizedClientRegistrationId();

      // ✅ 새로운 인증 객체 생성
      OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
          updatedUser,
          updatedUser.getAuthorities(),
          registrationId
      );

      // ✅ 세션 및 currentUserDTO 업데이트
      ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attr == null) return;
      HttpSession session = request.getSession(false);
      if (session == null) return;
      CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
      if (currentUser == null) return;

      PartnerInfo partnerInfo = partnerInfoRepository.findByMember_EmailId(member.getEmailId()).orElse(null);
      String bno = (partnerInfo != null) ? partnerInfo.getBno() : null;

      CurrentUserDTO updatedUserDTO = CurrentUserDTO.builder()
          .emailId(currentUser.getEmailId())
          .nickname(currentUser.getNickname())
          .role(member.getRole())
          .bno(bno)
          .build();

      session.setAttribute("currentUser", updatedUserDTO);

      // ✅ 인증 객체 교체
      SecurityContextHolder.getContext().setAuthentication(newAuth);

      // ✅ OAuth2AuthorizedClient 갱신 (중요!)
      authorizedClientRepository.saveAuthorizedClient(
          authorizedClientService.loadAuthorizedClient(registrationId, oauthToken.getName()),
          newAuth,
          request,
          response
      );
  }

  // 파트너 가게 이름 조회
  @Override
  public String getStoreNameByEmail(String emailId) {
    PartnerInfo partnerInfo = partnerInfoRepository.findByMember_EmailId(emailId)
        .orElseThrow(() -> new IllegalArgumentException("파트너 정보가 없습니다."));
    return partnerInfo.getName();
  }

  // 이메일로 파트너 코드 조회
  @Override
  public String getCodeByEmail(String emailId) {
    String code = partnerInfoRepository.findCodeByEmailId(emailId);
    if (code == null) {
      throw new IllegalArgumentException("파트너 코드가 없습니다.");
    }
    return code;
  }
}

