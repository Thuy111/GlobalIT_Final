package com.bob.service;

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
  default estimateDTO dtoToEntity(estimateDTO dto) {
    Estimate estimate = Estimate.builder()
        .id(dto.getId())
        .title(dto.getTitle())
        .content(dto.getContent())
        .price(dto.getPrice())
        .build();

    return estimate;
  }
}