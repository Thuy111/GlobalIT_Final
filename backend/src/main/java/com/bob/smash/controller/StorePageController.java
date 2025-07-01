package com.bob.smash.controller;

import com.bob.smash.dto.StorePageDTO;
import com.bob.smash.dto.StoreUpdateRequestDTO;
import com.bob.smash.service.StorePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/smash/store")
@RequiredArgsConstructor
public class StorePageController {
    
    private final StorePageService storeService;

    @GetMapping("/{code}")
    public StorePageDTO getStore(@PathVariable String code,
                                 @RequestParam String memberId) {
        return storeService.getStorePage(code, memberId);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStore(@ModelAttribute StoreUpdateRequestDTO dto) {
        storeService.updateStore(dto);
        return ResponseEntity.ok("수정 완료");
    }
}
