package com.bob.smash.service;

import com.bob.smash.dto.*;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.ProfileImage;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.ProfileImageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MemberRepository memberRepository;
    private final PartnerInfoRepository partnerInfoRepository;
    private final ProfileImageRepository profileImageRepository;

    @Value("${com.bob.upload.path}")
    private String uploadPath;

    // 이메일로 회원 정보 불러옴
    @Override
    public ProfileDTO getProfileByEmail(String emailId) {
        Member member = memberRepository.findByEmailId(emailId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        Optional<ProfileImage> profileOpt = profileImageRepository.findByMember(member);

        String profileImageUrl = profileOpt
                        .map(img -> "/uploads/" + img.getPath() + img.getSName())
                        .orElse(null);

        Optional<PartnerInfo> partnerOpt = partnerInfoRepository.findByMember_EmailId(emailId);

        byte role = member.getRole();

        boolean isPartner = partnerOpt.isPresent() && role == 1;

        System.out.println("isPartner?"+isPartner);
        System.out.println("PartnerOpt: " + partnerOpt.isPresent());
        System.out.println("Member Role: " + role);

        ProfileDTO.ProfileDTOBuilder builder = ProfileDTO.builder()
                .email(member.getEmailId())
                .nickname(member.getNickname())
                .loginType(member.getLoginType())
                .isPartner(isPartner)
                .profileImageUrl(profileImageUrl)
                .tel(member.getTel())
                .region(member.getRegion());

        partnerOpt.ifPresent(p -> {
            builder.bno(p.getBno());
            builder.partnerName(p.getName());
            builder.partnerTel(p.getTel());
            builder.partnerRegion(p.getRegion());
            builder.code(p.getCode());
        });

        return builder.build();
    }

    // 일반 유저 프로필 업데이트
    @Override
    @Transactional
    public void updateMember(String emailId, UpdateRequestDTO dto) {
        Member member = memberRepository.findByEmailId(emailId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        member.changeNickname(dto.getNickname());
        member.changeTel(dto.getTel());
        member.changeRegion(dto.getRegion());
    }

    // 파트너(업체) 유저 프로필 업데이트
    @Override
    @Transactional
    public void updatePartner(String emailId, UpdateRequestDTO dto) {
        updateMember(emailId, UpdateRequestDTO.builder()
                .nickname(dto.getNickname())
                .tel(dto.getTel())
                .region(dto.getRegion())
                .build());

        PartnerInfo partner = partnerInfoRepository.findByMember_EmailId(emailId)
                .orElseThrow(() -> new IllegalArgumentException("파트너 정보 없음"));
        partner.changeName(dto.getPartnerName());
        partner.changeTel(dto.getPartnerTel());
        partner.changeRegion(dto.getPartnerRegion());
    }

    // 닉네임 중복확인
    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 휴대폰 중복 검사
    @Override
    public boolean isPhoneValid(String phone) {
        return !memberRepository.findByTel(phone).isPresent();
    }

    // 이미지 파일 업로드
    @Override
    @Transactional
    public ProfileImageResponseDTO uploadProfileImage(String emailId, MultipartFile file) {
        Member member = memberRepository.findByEmailId(emailId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 기존 이미지 삭제
        profileImageRepository.findByMember(member).ifPresent(existing -> {

            // 실제 파일 전체 경로
                String fullPath = Paths.get(uploadPath, existing.getPath(), existing.getSName())
                        .toAbsolutePath().normalize().toString();
                log.info("삭제하려는 파일 경로: {}", fullPath);

                File existingFile = new File(fullPath);
                if (existingFile.exists()) {
                    if (existingFile.delete()) {
                        log.info("파일 삭제 성공: {}", fullPath);
                    } else {
                        log.warn("파일 삭제 실패: {}", fullPath);
                    }
                } else {
                    log.warn("파일이 존재하지 않음: {}", fullPath);
                }

                // 디렉토리 정리: 이미지가 있던 폴더 경로
                File parentDir = new File(Paths.get(uploadPath, existing.getPath()).toAbsolutePath().normalize().toString());
                log.info("삭제하려는 디렉토리 경로: {}", parentDir.getAbsolutePath());

                if (parentDir.exists() && parentDir.isDirectory()) {
                    String[] remainingFiles = parentDir.list();
                    if (remainingFiles != null && remainingFiles.length == 0) {
                        if (parentDir.delete()) {
                            log.info("빈 폴더 삭제 성공: {}", parentDir.getAbsolutePath());
                        } else {
                            log.warn("빈 폴더 삭제 실패: {}", parentDir.getAbsolutePath());
                        }
                    } else {
                        log.info("디렉토리 내에 다른 파일이 존재하여 폴더를 삭제하지 않음: {}", parentDir.getAbsolutePath());
                    }
                } else {
                    log.warn("디렉토리가 존재하지 않거나 폴더가 아님: {}", parentDir.getAbsolutePath());
                }
            profileImageRepository.delete(existing);
        });

        // 새로 저장
        String uuid = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String saveName = uuid + ext;

        // 날짜 + 시간 조합 (예: 20250706123045)
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());

        String envPath = "user/";
        String relativePath = envPath + member.getEmailId() + "_" + timestamp;
        String absUploadPath = Paths.get(uploadPath, "user", member.getEmailId() + "_" + timestamp).toAbsolutePath().toString();
        

    
        File dir = new File(absUploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File savePath = new File(dir, saveName);
        try {
            file.transferTo(savePath);
        } catch (IOException e) {
            log.error("파일 저장 실패 경로: {}", savePath.getAbsolutePath(), e);
            throw new RuntimeException("파일 저장 실패", e);
        }

        String urlPath = relativePath + "/" + saveName;

        ProfileImage profileImage = ProfileImage.builder()
                .member(member)
                .sName(saveName)
                .oName(originalName)
                .path(relativePath + "/")
                .type(file.getContentType())
                .size(file.getSize())
                .build();

        profileImageRepository.save(profileImage);

        return new ProfileImageResponseDTO(urlPath);
    }

    // 이미지 삭제
    @Override
    @Transactional
    public void deleteProfileImage(String emailId) {
        Member member = memberRepository.findByEmailId(emailId)
        .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        Optional<ProfileImage> imageOpt = profileImageRepository.findByMember(member);

        imageOpt.ifPresent(profileImage -> {
            // 실제 파일 전체 경로
            String fullPath = Paths.get(uploadPath, profileImage.getPath(), profileImage.getSName())
                    .toAbsolutePath().normalize().toString();
            log.info("삭제하려는 파일 경로: {}", fullPath);

            File file = new File(fullPath);
            if (file.exists()) {
                if (file.delete()) {
                    log.info("파일 삭제 성공: {}", fullPath);
                } else {
                    log.warn("파일 삭제 실패: {}", fullPath);
                }
            } else {
                log.warn("파일이 존재하지 않음: {}", fullPath);
            }

            // 디렉토리 정리: 이미지가 있던 폴더 경로
            File parentDir = new File(Paths.get(uploadPath, profileImage.getPath()).toAbsolutePath().normalize().toString());
            log.info("삭제하려는 디렉토리 경로: {}", parentDir.getAbsolutePath());

            if (parentDir.exists() && parentDir.isDirectory()) {
                String[] remainingFiles = parentDir.list();
                if (remainingFiles != null && remainingFiles.length == 0) {
                    if (parentDir.delete()) {
                        log.info("빈 폴더 삭제 성공: {}", parentDir.getAbsolutePath());
                    } else {
                        log.warn("빈 폴더 삭제 실패: {}", parentDir.getAbsolutePath());
                    }
                } else {
                    log.info("디렉토리 내에 다른 파일이 존재하여 폴더를 삭제하지 않음: {}", parentDir.getAbsolutePath());
                }
            } else {
                log.warn("디렉토리가 존재하지 않거나 폴더가 아님: {}", parentDir.getAbsolutePath());
            }

            // 프로필 이미지 데이터베이스에서 삭제
            profileImageRepository.deleteById(emailId);
        });
    }
}