package com.bob.smash.service;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Request;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;

    // 등록
    @Override
    public Integer register(RequestDTO dto, Member member) {
    Request entity = dtoToEntity(dto, member);
    Request saved = requestRepository.save(entity);
    return saved.getIdx();

    }

    // 상세 조회
    @Override
    public RequestDTO get(Integer idx) {    
        Optional<Request> result = requestRepository.findById(idx);
        return result.map(this::entityToDto).orElse(null);
    }

    // 전체 목록 조회
    @Override
    public List<RequestDTO> getList() {   
        List<Request> list = requestRepository.findAll();
        return list.stream().map(this::entityToDto).collect(Collectors.toList());
    }
}
