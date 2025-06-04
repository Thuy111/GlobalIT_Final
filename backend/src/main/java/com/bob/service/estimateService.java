package com.bob.service;

import java.util.List;

import com.bob.smash.dto.estimateDTO;
import com.bob.smash.entity.Estimate;

public interface estimateService {
  // 등록
  Long register(estimateDTO dto);
  // 조회
  estimateDTO get(Long id);
  // 목록
  List<estimateDTO> getList();
  // 수정
  Long modify(estimateDTO dto);
  // 삭제
  void remove(Long id);

  // dto -> entity
  default Estimate dtoToEntity(estimateDTO dto) {
    Estimate estimate = Estimate.builder()
        .idx(dto.getIdx())
        .title(dto.getTitle())
        .content(dto.getContent())
        .price(dto.getPrice())
        .build();

    return estimate;
  }

  // entity -> dto
  default estimateDTO entityToDto(Estimate estimate) {
    estimateDTO dto = estimateDTO.builder()
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