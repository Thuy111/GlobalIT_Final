package com.bob.smash.service;

import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.entity.Request;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    public List<RequestListDTO> getRequestList() {
        return requestRepository.findAll()
                .stream()
                .map(request -> new RequestListDTO(
                        request.getIdx(),
                        request.getTitle(),
                        request.getCreatedAt().toLocalDate() // LocalDate로 변환
                ))
                .collect(Collectors.toList());
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public Request getById(Integer id) {
        return requestRepository.findById(id).orElse(null);
    }
}
