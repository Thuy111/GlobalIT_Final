package com.bob.smash.service;

import com.bob.smash.dto.*;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    // 이메일을 이용해 해당 유저의 프로필 정보를 불러옴
    ProfileDTO getProfileByEmail(String emailId);

    // 일반 유저 정보 업데이트
    void updateMember(String emailId, UpdateRequestDTO dto);

    // 업체 회원 정보 업데이트
    void updatePartner(String emailId, UpdateRequestDTO dto);

    // 닉네임 중복체크
    boolean isNicknameDuplicated(String nickname);

    // 핸드폰 유효성 + 중복 체크
    boolean isPhoneValid(String phone);

    // 프로필 이미지 업로드
    ProfileImageResponseDTO uploadProfileImage(String emailId, MultipartFile file);

    // 프로필 이미지 삭제
    void deleteProfileImage(String emailId);
}
