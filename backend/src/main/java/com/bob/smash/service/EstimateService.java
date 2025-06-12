package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Request;

public interface EstimateService {
  // 등록
  Integer register(EstimateDTO dto);
  // 목록
  List<EstimateDTO> getList();
  // 반납 현황 수정
  Integer returnStatus(EstimateDTO dto);
  // 조회
  EstimateDTO get(Integer idx);
  // 수정
  Integer modify(EstimateDTO dto);
  // 삭제
  void remove(Integer idx);

  // dto -> entity
  default Estimate dtoToEntity(EstimateDTO dto) {
    Estimate estimate = Estimate.builder()
                                .idx(dto.getIdx())
                                .request(Request.builder().idx(dto.getRequestIdx()).build())
                                .partnerInfo(PartnerInfo.builder().bno(dto.getPartnerBno()).build())
                                .title(dto.getTitle())
                                .content(dto.getContent())
                                .price(dto.getPrice())
                                .createdAt(dto.getCreatedAt())
                                .returnDate(dto.getReturnDate())
                                .isDelivery(Boolean.TRUE.equals(dto.getIsDelivery()) ? (byte) 1 : (byte) 0)
                                .isPickup(Boolean.TRUE.equals(dto.getIsPickup()) ? (byte) 1 : (byte) 0)
                                .isSelected(dto.getIsSelected())
                                .isReturn(Boolean.TRUE.equals(dto.getIsReturn()) ? (byte) 1 : (byte) 0)
                                .build();
    return estimate;
  }

  // entity -> dto
  default EstimateDTO entityToDto(Estimate estimate) {
    EstimateDTO dto = EstimateDTO.builder()
                                 .idx(estimate.getIdx())
                                 .title(estimate.getTitle())
                                 .content(estimate.getContent())
                                 .price(estimate.getPrice())
                                 .createdAt(estimate.getCreatedAt())
                                 .returnDate(estimate.getReturnDate())
                                 .isDelivery(estimate.getIsDelivery() == 1)
                                 .isPickup(estimate.getIsPickup() == 1)
                                 .isSelected(estimate.getIsSelected())
                                 .isReturn(estimate.getIsReturn() == 1)
                                 .requestIdx(estimate.getRequest().getIdx())
                                 .requestMemberId(estimate.getRequest().getMember().getEmailId())
                                 .partnerBno(estimate.getPartnerInfo().getBno())
                                 .partnerName(estimate.getPartnerInfo().getName())
                                 .partnerTel(estimate.getPartnerInfo().getTel())
                                 .partnerRegion(estimate.getPartnerInfo().getRegion())
                                 .build();
    return dto;
  }
}
