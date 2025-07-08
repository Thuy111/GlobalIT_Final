package com.bob.smash.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
public class CsrfController {
    @GetMapping("/api/csrf")
    public CsrfToken csrfToken(CsrfToken token) {
        return token;
    }
}