package com.bob.smash.service;

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.ProfileImage;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.ProfileImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MemberRepository memberRepository;
    private final PartnerInfoRepository partnerInfoRepository;
    private final ProfileImageRepository profileImageRepository;

    private final String uploadPath = "uploads"; // application.properties와 일치

    @Override
    public ProfileDTO getProfileByEmail(String emailId) {
        Member member = memberRepository.findByEmailId(emailId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        Optional<ProfileImage> profileOpt = profileImageRepository.findById(member.getEmailId());
        String profileImageUrl = profileOpt.map(img -> img.getPath() + "/" + img.getSName()).orElse(null);

        Optional<PartnerInfo> partnerOpt = partnerInfoRepository.findByMember_EmailId(emailId);
        boolean isPartner = partnerOpt.isPresent();

        ProfileDTO.ProfileDTOBuilder builder = ProfileDTO.builder()
                .email(member.getEmailId())
                .nickname(member.getNickname())
                .loginType(member.getLoginType())
                .isPartner(isPartner)
                .profileImageUrl(profileImageUrl);

        partnerOpt.ifPresent(p -> {
            builder.bno(p.getBno());
            builder.partnerName(p.getName());
        });

        return builder.build();
    }


}