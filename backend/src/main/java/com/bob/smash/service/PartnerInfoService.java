package com.bob.smash.service;

import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.dto.PartnerVerificationResponseDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;

public interface PartnerInfoService {
  // 파트너 정보 조회 : 이메일
  PartnerInfoDTO getPartnerInfo(String emailId);
  // 파트너 정보 조회 : 사업자 번호
  PartnerInfoDTO getPartnerInfoByBno(String bno);
  // 파트너 삭제
  void deleteByMemberEmail(String email);
  // 유저 -> 파트너 전환 할 때 bno 검증
  PartnerVerificationResponseDTO verifyAndRegister(String emailId, PartnerInfoDTO dto);
  // 파트너 -> 유저 로 role 변경 
  void convertToUser(String emailId);
  // 유저 -> 파트너 전환 role 변경
  void updateRoleIfPartnerInfoExists(String emailId);
  // 파트너 가게 이름 조회
  public String getStoreNameByEmail(String emailId);
  // 이메일로 파트너 코드 조회
  String getCodeByEmail(String emailId);

  // Dto → Entity 변환
  default PartnerInfo dtoToEntity(PartnerInfoDTO dto) {
    Member member = Member.builder().emailId(dto.getMemberId()).build();
    return PartnerInfo.builder()
                      .bno(dto.getBno())
                      .name(dto.getName())
                      .tel(dto.getTel())
                      .region(dto.getRegion())
                      .description(dto.getDescription())
                      .visitCnt(dto.getVisitCnt())
                      .member(member)
                      .build();
  }
  // Entity → Dto 변환
  default PartnerInfoDTO entityToDto(PartnerInfo entity) {
    return PartnerInfoDTO.builder()
                         .bno(entity.getBno())
                         .name(entity.getName())
                         .tel(entity.getTel())
                         .region(entity.getRegion())
                         .description(entity.getDescription())
                         .visitCnt(entity.getVisitCnt())
                         .memberId(entity.getMember().getEmailId())
                         .build();
  }
}
