package com.bob.smash.controller;

import com.bob.smash.dto.RequestListDTO;
import com.bob.smash.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.bob.smash.entity.Request;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public List<RequestListDTO> getAllRequests() {
        return requestService.getRequestList();
    }

    @PostMapping
    public Request createRequest(@RequestBody Request request) {
        return requestService.save(request);
    }

    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Integer id) {
        return requestService.getById(id);
    }
}
