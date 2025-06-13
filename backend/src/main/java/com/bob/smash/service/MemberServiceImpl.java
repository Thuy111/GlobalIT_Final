package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.bob.smash.dto.MemberDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
  private final MemberRepository memberRepository;

  @Value("${front.server.url}")
  private String frontServerUrl;

  //  소셜로그인 이메일을 통한 유저정보 DTO 반환
  @Override
  public MemberDTO getCurrentUser(OAuth2User user,  Map<String, String> body) {
        System.out.println("user = " + user);
        if (user == null) {
            throw new IllegalArgumentException("로그인 정보가 없습니다.");
        }

        // OAuth2User에서 필요한 정보 추출 (카카오, 구글)
        Map<String, Object> attributes = user.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = null;
        if (kakaoAccount != null) { // 카카오 로그인인 경우
            email = (String) kakaoAccount.get("email");
        } else { // 구글 로그인인 경우
            email = (String) attributes.get("email"); 
        }
        System.out.println("email = " + email);

        Optional<Member> memberOpt = memberRepository.findByEmailId(email);

        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("가입되지 않은 계정입니다.");
        }

        Member member = memberOpt.get();
        System.out.println("member = " + member);

        // 필요한 정보를 DTO로 변환하여 반환
        return entityToDto(member);
  }

  // 소셜 로그인 후 전화번호 등록
  @Override
  public void registerPhoneNumber(HttpServletRequest request, Map<String, String> body){
        String email = body.get("email");
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("전화번호가 누락되었습니다.");
        }

        System.out.println("email = " + email);

        Optional<Member> memberOpt = memberRepository.findByEmailId(email);

        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("가입되지 않은 계정입니다.");
        }

        Member member = memberOpt.get();
        member.setTel(phone);
        memberRepository.save(member);
  }

  @Override
  public void checkUser(OAuth2User user, HttpServletRequest request) { // (가입된 번호, DB 이메일 조회) 유효성 체크
        if (user == null) {
            System.out.println("!!! 유저 정보가 없습니다. ::: 401 ERROR !!!");
            // 유저 정보가 없을 경우, 인증 실패로 처리
            // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // OAuth2User에서 필요한 정보 추출 (카카오, 구글)
        Map<String, Object> attributes = user.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = null;
        if (kakaoAccount != null) { // 카카오 로그인인 경우
            email = (String) kakaoAccount.get("email");
        } else { // 구글 로그인인 경우
            email = (String) attributes.get("email"); 
        }
        System.out.println("email = " + email);
        
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("가입되지 않은 계정입니다.");
        }
        Optional<Member> memberOpt = memberRepository.findByEmailId(email);

        Member member = memberOpt.get();
        if (member.getTel() == null) {
            throw new IllegalArgumentException("번호가 등록되지 않은 계정입니다.");
        }
        
        if (memberOpt.isEmpty()) {
            // DB에 해당 이메일이 없을 때
            request.getSession().invalidate();// 세션 파기
            // 소셜로그인 연동 해제

            throw new IllegalArgumentException("가입되지 않은 계정입니다.");
        }
    }

  @Override
  public void completeSocialSignup(HttpServletRequest request, Map<String, String> body) {
        String email = body.get("email");
        String provider = body.get("provider");
        String nickname = body.get("nickname");
        String phone = body.get("phone");

        if (email == null || provider == null || phone == null) {
            throw new IllegalArgumentException("필수 정보가 누락되었습니다.");
        }

        // Optional<Member> existingMember = memberRepository.findByEmailId(email);
        // 전화번호 중복으로 가입이력을 확인
        Optional<Member> existingByTel = memberRepository.findByTel(phone);
        if (existingByTel.isPresent()) {
            // 로그인 타입을 이메일과 전화번호로 둘 다 확인
            String loginType = existingByTel.get().getLoginType().toString();
            // 세션 파기
            request.getSession().invalidate();
            // 소셜로그인 연동 해제


            throw new DuplicateMemberException(loginType + "로 이미 가입된 회원입니다." + loginType + "로 로그인해주세요.");
        }

        Member.LoginType loginType = "kakao".equals(provider) ?
                Member.LoginType.kakao : Member.LoginType.google;

        Member newMember = Member.builder()
                .emailId(email)
                .nickname(nickname)
                .createdAt(LocalDateTime.now())
                .loginType(loginType)
                .role((byte) 0)
                .tel(phone)
                .build();

        memberRepository.save(newMember);
    }

    public class DuplicateMemberException extends RuntimeException {
        public DuplicateMemberException(String message) {
            super(message);
        }
    }
}
