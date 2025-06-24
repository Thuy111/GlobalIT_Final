package com.bob.smash.service;

import com.bob.smash.dto.*;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileDTO getProfileByEmail(String emailId);
    void updateMember(String emailId, UpdateRequestDTO dto);
    void updatePartner(String emailId, UpdateRequestDTO dto);
    boolean isNicknameDuplicated(String nickname);
    boolean isPhoneValid(String phone);
    ProfileImageResponseDTO uploadProfileImage(String emailId, MultipartFile file);
    void deleteProfileImage(String emailId);
}
