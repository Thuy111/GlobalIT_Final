package com.bob.smash.service;

import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;

    public Page<RequestListDTO> getRequestList(int page, int size) {
        return requestRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(r -> new RequestListDTO(
                        r.getIdx(),
                        r.getTitle(),
                        r.getCreatedAt().toLocalDate()
                ));
    }
}
