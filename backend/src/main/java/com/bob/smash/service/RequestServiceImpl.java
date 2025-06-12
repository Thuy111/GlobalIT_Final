package com.bob.smash.service;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Request;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;
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

    // 무한스크롤용 페이지네이션 기능 구현
    @Override
    public Map<String, Object> getPagedRequestList(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Request> requestPage;

        if (search != null && !search.isBlank()) {
            requestPage = requestRepository.findByTitleContaining(search, pageable);
        } else {
            requestPage = requestRepository.findAll(pageable);
        }

        List<RequestListDTO> requestDTOs = requestPage.getContent()
                .stream()
                .map(request -> {
                    LocalDate createdAt = request.getCreatedAt().toLocalDate();
                    LocalDate useDate = request.getUseDate().toLocalDate(); 
                    String dDay = calculateDDay(createdAt, useDate);

                    return new RequestListDTO(
                        request.getIdx(),
                        request.getTitle(),
                        request.getContent(),
                        request.getIsDone(),
                        createdAt,
                        useDate,
                        dDay
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("request", requestDTOs);
        response.put("currentPage", page);
        response.put("totalPages", requestPage.getTotalPages());
        response.put("hasNext", requestPage.hasNext());

        return response;
    }

    // D-DAY 계산 함수
    private String calculateDDay(LocalDate createdAt, LocalDate useDate) {
        long days = ChronoUnit.DAYS.between(createdAt, useDate);
        if (days == 0) return "D-DAY";
        else if (days > 0) return "D-" + days;
        else return "D+" + Math.abs(days);
    }
}
