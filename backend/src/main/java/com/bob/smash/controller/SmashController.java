package com.bob.smash.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bob.smash.dto.ThemeDTO;

import jakarta.servlet.http.HttpSession;
// import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; 

@Controller
@RequestMapping("/smash")
// @RequiredArgsConstructor // 자동주입
@Log4j2
public class SmashController {  
  // service 주입

  // application.properties에서 Frontend URL 설정
  @Value("${front.server.url}")
  private String frontendUrl;

  @GetMapping({"","/"})
  public String index() {
    log.info(frontendUrl + "로 리다이렉트합니다.");
    return "redirect:" + frontendUrl;
  }

  @GetMapping("/estimate")
  public String estimate() {
    return "redirect:/smash/estimate/all";
  }

  @GetMapping("/request")
  public String request() {
    log.info("Smash request page requested");
    return "redirect:/smash/request/list";
  }

  @GetMapping("/profile/update")
  public void profileUpdate() {
    log.info("Smash profile update page requested");
  }

  // theme
  @ResponseBody
  @PostMapping("/theme")
  public void setTheme(@RequestBody ThemeDTO themeDTO, HttpSession session) {
    String theme = themeDTO.getTheme();
    session.setAttribute("theme", theme);
    System.out.println("테마는 ==========" + theme);
  }
}
