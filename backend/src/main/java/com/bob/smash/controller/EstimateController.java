package com.bob.smash.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bob.smash.service.EstimateService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/estimate")
public class EstimateController {
  private final EstimateService service;

  @Value("${front.server.url}")
  private String frontendUrl;

  @GetMapping("/")
  public String index() {
    log.info("Smash index page requested");
    return "redirect:" + frontendUrl + "/";
  }
}