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

  @Override
  public Integer register(EstimateDTO dto) {
    Estimate estimate = dtoToEntity(dto);
    repository.save(estimate);
    return estimate.getIdx();
  }

  @Override
  public EstimateDTO get(Long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'get'");
  }

  @Override
  public List<EstimateDTO> getList() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getList'");
  }

  @Override
  public Long modify(EstimateDTO dto) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'modify'");
  }

  @Override
  public void remove(Long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }
  
}