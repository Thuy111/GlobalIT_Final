package com.bob.smash.service;

import com.bob.smash.dto.StorePageDTO;
import com.bob.smash.dto.StoreUpdateRequestDTO;

public interface StorePageService {
    StorePageDTO getStorePage(String code, String loggedInMemberId);
    void updateStore(StoreUpdateRequestDTO dto);
}
