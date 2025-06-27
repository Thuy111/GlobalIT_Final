package com.bob.smash.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.dto.PartnerVerificationResponseDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.PaymentRepository;

import jakarta.transaction.Transactional;
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
    }
  }
}
