package com.bob.smash.service;

import org.springframework.stereotype.Service;

import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.repository.PartnerInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerInfoServiceImpl implements PartnerInfoService {
  private final PartnerInfoRepository partnerInfoRepository;
  
  // 파트너 정보 조회
  @Override
  public PartnerInfoDTO getPartnerInfo(String emailId){
    PartnerInfo partnerInfo = partnerInfoRepository.findByMemberEmailId(emailId).orElse(null);
    if (partnerInfo == null) {
      return null; // 파트너 정보가 없으면 null 반환
    }
    return entityToDto(partnerInfo);
  }

  // 파트너 삭제
  @Override
  public void deleteByMemberEmail(String email) {
    partnerInfoRepository.deleteByMemberEmailId(email);
  }

}
