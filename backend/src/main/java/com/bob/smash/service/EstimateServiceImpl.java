package com.bob.smash.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.repository.EstimateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstimateServiceImpl implements EstimateService {
  private final EstimateRepository repository;

  // 등록
  @Override
  public Integer register(EstimateDTO dto) {
    Estimate estimate = dtoToEntity(dto);
    repository.save(estimate);
    return estimate.getIdx();
  }

  // 조회
  @Override
  public EstimateDTO get(Integer idx) {
    Estimate estimate = repository.findById(idx)
                                  .orElseThrow(() -> new IllegalArgumentException(idx+"번 견적서를 찾을 수 없습니다."));
    return entityToDto(estimate);
  }

  // 목록
  @Override
  public List<EstimateDTO> getList() {
    List<Estimate> result = repository.findAll();
    return result.stream().map(estimate -> entityToDto(estimate)).toList();
  }

  // 수정
  @Override
  public Integer modify(EstimateDTO dto) {
    throw new UnsupportedOperationException("Unimplemented method 'modify'");
  }

  // 삭제
  @Override
  public void remove(Integer idx) {
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }
}