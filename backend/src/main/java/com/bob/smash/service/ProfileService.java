package com.bob.smash.service;

import com.bob.smash.dto.ProfileDTO;

public interface ProfileService {
    ProfileDTO getProfileByEmail(String emialId);
}
