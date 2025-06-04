package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Request;

public interface EstimateService {
  // 등록
  Integer register(EstimateDTO dto);
  // 조회
  EstimateDTO get(Integer idx);
  // 목록
  List<EstimateDTO> getList();
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
        .isSelected(dto.getIsSelected() ? (byte) 1 : (byte) 0)
        .isReturn(dto.getIsReturn() ? (byte) 1 : (byte) 0)
        .build();

    return estimate;
  }

  // entity -> dto
  default EstimateDTO entityToDto(Estimate estimate) {
    EstimateDTO dto = EstimateDTO.builder()
        .idx(estimate.getIdx())
        .requestIdx(estimate.getRequest().getIdx())
        .partnerBno(estimate.getPartnerInfo().getBno())
        .title(estimate.getTitle())
        .content(estimate.getContent())
        .price(estimate.getPrice())
        .createdAt(estimate.getCreatedAt())
        .returnDate(estimate.getReturnDate())
        .isSelected(estimate.getIsSelected() == 1)
        .isReturn(estimate.getIsReturn() == 1)
        .build();

    return dto;
  }
}