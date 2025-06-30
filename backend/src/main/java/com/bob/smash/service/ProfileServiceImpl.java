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
                        .map(img -> "/uploads/" + img.getSName())
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
            profileImageRepository.delete(existing);
        });

        // 새로 저장
        String uuid = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String saveName = uuid + ext;

        String absUploadPath = Paths.get(uploadPath).toAbsolutePath().toString();
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

        String urlPath = "/uploads/" + saveName;

        ProfileImage profileImage = ProfileImage.builder()
                .member(member)
                .sName(saveName)
                .oName(originalName)
                .path("/uploads")
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
        profileImageRepository.deleteById(emailId);
        // 실제 파일도 지울 수 있음 (선택)
    }
}