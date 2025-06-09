package com.bob.smash.service;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.entity.Image;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Request;
// import com.bob.smash.repository.ImageRepository;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository; // 주입
    // private final ImageRepository imageRepository;

    @Override
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

    @Override
    public Integer register(RequestDTO requestDTO, Member member) {

        // 내용  등록
        Request request = dtoToEntity(requestDTO, member);
        Request saved = requestRepository.save(request);
        return saved.getIdx();


        // 1. 저장할 파일이 존재하면 이미지 먼저 저장
        // MultipartFile file = requestDTO.getImageFile();
        // if (file != null && !file.isEmpty()) {
        //     try {
        //         String uuid = UUID.randomUUID().toString();
        //         String originalName = file.getOriginalFilename();
        //         String sName = uuid + "_" + originalName;
        //         String uploadDir = "uploads/"; // 저장 디렉토리
        //         File saveFile = new File(uploadDir + sName);
                
        //         // 디렉토리가 없다면 생성
        //         saveFile.getParentFile().mkdirs();

        //         // 파일 저장
        //         file.transferTo(saveFile);

        //         // 이미지 DB 저장
        //         Image image = Image.builder()
        //                 .sName(sName)
        //                 .oName(originalName)
        //                 .path(uploadDir + sName)
        //                 .type(file.getContentType())
        //                 .size((int) file.getSize())
        //                 .build();
        //         imageRepository.save(image);

        //     } catch (IOException e) {
        //         e.printStackTrace();
        //         // 예외 처리 로직 필요 (예: throw new RuntimeException("파일 저장 실패"))
        //     }
        // }

        
    }
}
