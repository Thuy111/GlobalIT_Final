package com.bob.smash.service;

import org.springframework.stereotype.Service;

import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerInfoServiceImpl implements PartnerInfoService {

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

}
