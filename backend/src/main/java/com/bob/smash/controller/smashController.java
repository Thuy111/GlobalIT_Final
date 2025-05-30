package com.bob.smash.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/smash")
// @RequiredArgsConstructor // 자동주입
@Log4j2
public class smashController {
  // service 주입

  // .env에서 Frontend URL 설정
  @Value("${front.server.url}")
  private String frontendUrl;

  @GetMapping("/")
  public String index() {
    log.info("Smash index page requested");
    return "redirect:" + frontendUrl + "/";
  }

  @GetMapping("/test")
  public String test() {
    log.info("Smash test page requested");
    return "redirect:/smash/test";
  }

}
