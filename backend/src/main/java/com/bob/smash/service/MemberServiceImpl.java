package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bob.smash.dto.MemberDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Member.LoginType;
import com.bob.smash.exception.DuplicateMemberException;
import com.bob.smash.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
  private final MemberRepository memberRepository;

  @Value("${front.server.url}")
  private String frontServerUrl;

  //  소셜로그인 이메일을 통한 유저정보 DTO 반환
  @Override
  public MemberDTO getCurrentUser(OAuth2AuthenticationToken authentication) {
    if (authentication == null) {
        throw new IllegalArgumentException("로그인 정보가 없습니다.");
    }

    OAuth2User user = authentication.getPrincipal();
    Map<String, Object> attributes = user.getAttributes();

    String email = null;
    String registrationId = authentication.getAuthorizedClientRegistrationId(); // "kakao", "google"

    if ("kakao".equals(registrationId)) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        email = (String) kakaoAccount.get("email");
    } else if ("google".equals(registrationId)) {
        email = (String) attributes.get("email");
    }

    if (email == null) {
        throw new IllegalArgumentException("이메일 정보를 찾을 수 없습니다.");
    }

    Optional<Member> memberOpt = memberRepository.findByEmailId(email);
    if (memberOpt.isEmpty()) {
        throw new IllegalArgumentException("가입되지 않은 계정입니다.");
    }

    MemberDTO dto = entityToDto(memberOpt.get());

    // 문자열을 enum으로 안전하게 변환
    dto.setLoginType(LoginType.valueOf(registrationId));

    return dto;
  }

  // 소셜 로그인 후 전화번호 등록
  @Override
  public void registerPhoneNumber(HttpServletRequest request, Map<String, String> body){
        String email = body.get("email");
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("전화번호가 누락되었습니다.");
        }

        // System.out.println("email = " + email);

        Optional<Member> memberOpt = memberRepository.findByEmailId(email);

        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("가입되지 않은 계정입니다.");
        }

        Member member = memberOpt.get();
        member.changeTel(phone);
        memberRepository.save(member);
  }

  @Override
  public void checkUser(OAuth2AuthenticationToken user, HttpServletRequest request) { // (가입된 번호, DB 이메일 조회) 유효성 체크
        if (user == null) {
            System.out.println("!!! 유저 정보가 없습니다. ::: 401 ERROR !!!");
            // 유저 정보가 없을 경우, 인증 실패로 처리
            // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // OAuth2AuthenticationToken에서 필요한 정보 추출 (카카오, 구글)
        OAuth2User oauthUser = user.getPrincipal(); // 유저 객체 추출
        Map<String, Object> attributes = oauthUser.getAttributes(); // 이제 안전하게 attributes 접근 가능
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = null;
        if (kakaoAccount != null) { // 카카오 로그인인 경우
            email = (String) kakaoAccount.get("email");
        } else { // 구글 로그인인 경우
            email = (String) attributes.get("email"); 
        }
        // System.out.println("email = " + email);
        
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

  // 소셜 로그인 회원가입 완료 + 중복 처리
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

        // .save()에 Transactional이 적용되어 있어 자동으로 커밋됨
        // (단, 예외 발생 시 롤백되므로 여러 save 호출시에는 명시적으로 @Transactional을 사용해야 함)
        memberRepository.save(newMember); 
    }

    // 카카오 회원 탈퇴 및 연동 해제
    @Transactional
    @Override
    public void unlinkAndDeleteKakaoMember(String accessToken, MemberDTO currentUser) {
        // 1. 카카오 unlink 요청 보내기
        WebClient.create()
                .post()
                .uri("https://kapi.kakao.com/v1/user/unlink")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리 (필요 시 비동기로 바꿔도 됨)

        // 2. 사용자 이메일 가져오기
        // System.out.println("탈퇴 대상 ID = " + currentUser.getEmailId());
        String email = currentUser.getEmailId();
        System.out.println("email = " + email);
        if (email == null) {
            throw new IllegalStateException("카카오 계정에서 이메일 정보를 가져올 수 없습니다.");
        }

        // 4. 이메일 기준으로 사용자 삭제
        memberRepository.deleteByEmailId(email);
    }

    // 구글 회원 탈퇴 및 연동 해제
    @Transactional
    @Override
    public void unlinkAndDeleteGoogleMember(String accessToken, MemberDTO currentUser) {
        // 1. 토큰 폐기 요청 (revoke)
        WebClient.create()
            .post()
            .uri("https://oauth2.googleapis.com/revoke?token=" + accessToken)
            .retrieve()
            .bodyToMono(Void.class)
            .block();

        // 2. 사용자 이메일로 삭제
        String email = currentUser.getEmailId();
        System.out.println("email = " + email);
        if (email == null) {
            throw new IllegalStateException("구글 사용자 이메일이 존재하지 않습니다.");
        }

        memberRepository.deleteByEmailId(email);
    }
}
