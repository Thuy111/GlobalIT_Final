package com.bob.smash.service;

// 추후 DTO 통합
import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.entity.Request;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Member;

import java.util.List;
import java.util.stream.Collectors;

@Service
public interface RequestService {

    // Request List 조회
    List<RequestListDTO> getRequestList();

    // Request 등록
    Integer register(RequestDTO requestDTO, Member member);

    // DTO → Entity
    default Request dtoToEntity(RequestDTO dto, Member member) {
        return Request.builder()
                .member(member)
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdAt(java.time.LocalDateTime.now())
                .useDate(dto.getUseDate())
                .useRegion(dto.getUseRegion())
                // .isDelivery(dto.getIsDelivery())
                .isDone((byte) 0)
                .isGet((byte) 0)
                .build();
    }

    // Entity → DTO
    default RequestDTO entityToDto(Request entity) {
        return RequestDTO.builder()
                .idx(entity.getIdx())
                .title(entity.getTitle())
                .content(entity.getContent())
                .useDate(entity.getUseDate())
                .useRegion(entity.getUseRegion())
                // .isDelivery(entity.getIsDelivery())
                .emailId(entity.getMember().getEmailId())
                .build();
    }
}
