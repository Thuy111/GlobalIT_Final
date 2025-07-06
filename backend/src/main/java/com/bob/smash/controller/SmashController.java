package com.bob.smash.controller;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.bob.smash.service.ContactMailService;

import com.bob.smash.dto.ThemeDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; 

@Controller
@RequestMapping("/smash")
@RequiredArgsConstructor // 자동주입
@Log4j2
public class SmashController {
  private final ContactMailService contactMailService;

  // application.properties에서 Frontend URL 설정
  @Value("${front.server.url}")
  private String frontendUrl;

  @Value("${spring.mail.username}")
  private String aboutEmail;
  @Value("${adminTel}")
  private String aboutTel;

  @GetMapping({"","/"})
  public String index() {
    log.info(frontendUrl + "로 리다이렉트합니다.");
    return "redirect:" + frontendUrl;
  }

  @GetMapping("/request")
  public String request() {
    return "redirect:/smash/request/";
  }

  @GetMapping("/estimate")
  public String estimate() {
    return "redirect:/smash/estimate/";
  }

  @GetMapping("/review")
  public String review() {
    return "redirect:/smash/review/";
  }

  @GetMapping("/profile/update")
  public void profileUpdate() {
    log.info("Smash profile update page requested");
  }

  // contact
  @GetMapping("/contact")
  @ResponseBody
  public ResponseEntity<String> about(){
    // Json으로 about 정보를 반환
    String aboutInfo = "{ \"email\": \"" + aboutEmail + "\", \"tel\": \"" + aboutTel + "\" }";
    return ResponseEntity.ok(aboutInfo);
  }
  @PostMapping("/contact")
  @ResponseBody
  public ResponseEntity<String> sendContact(
          @RequestParam String name,
          @RequestParam String phone,
          @RequestParam String email,
          @RequestParam String message,
          @RequestParam(value = "files", required = false) List<MultipartFile> files
  ) throws MessagingException, IOException {
    try{
      // 메일 전송 서비스 실행 (파일 첨부 포함)
      contactMailService.sendContactMail(name, phone, email, message, files);
      return ResponseEntity.ok("success");
    } catch (MessagingException e) {
      System.out.println("파일 처리 실패: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 전송 실패");
    } catch (IOException e) {
      System.out.println("파일 처리 실패: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 실패");
    }

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