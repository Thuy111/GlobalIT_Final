package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.MemberDTO;
import com.bob.smash.dto.PartnerInfoDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Member.LoginType;
import com.bob.smash.exception.DuplicateMemberException;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.service.ProfileService;
// import com.bob.smash.repository.ReviewRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private static final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MemberRepository memberRepository;
    private final PartnerInfoService partnerInfoService;
    private final EstimateService estimateService;
    private final RequestService requestService;
    private final ProfileService profileService;
    private final NotificationService notificationService;
    private final HttpSession session;
    // private final ReviewRepository reviewRepository;

  // 소셜로그인 이메일을 통한 유저정보 DTO 반환
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
    if (memberOpt.isEmpty()){
        unlinkSocial(); // 소셜 로그인 연동 해제
        return null; // 회원 정보가 없으면 null 반환
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
        
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일 정보가 없습니다.");
        }

        Optional<Member> memberOpt = memberRepository.findByEmailId(email);
       
        Member member = memberOpt.orElse(null);
        
        System.out.println("=====>>> 회원 정보 조회: " + memberOpt.isPresent() + ", 이메일: " + email);
        if (memberOpt.isEmpty()) {// DB에 해당 이메일이 없을 때
            unlinkSocial(); // 소셜로그인 연동 해제
            request.getSession().invalidate(); // 세션 파기
            
            throw new IllegalArgumentException("가입되지 않은 계정입니다.");
        } else {
            System.out.println("=====>>> 회원은 존재함 → " + memberOpt.get().getEmailId() + ", 전화번호: " + memberOpt.get().getTel());
            // unreadAlarm 최신화
            HttpSession session = request.getSession();
            CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
            if (currentUser != null) {
                int unreadAlarm = notificationService.countUnreadNotifications(email, false); // 최신 unreadAlarm 값 조회
                currentUser.setUnreadAlarm(unreadAlarm); // DTO에 반영
                session.setAttribute("currentUser", currentUser); // 세션에 다시 저장
            }
        }

        if (member.getTel() == null) {
            throw new IllegalArgumentException("번호가 등록되지 않은 계정입니다.");
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
            unlinkSocial(); // 소셜로그인 연동 해제
            request.getSession().invalidate(); // 세션 파기

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

        // 세션에 사용자 정보 저장
        saveCurrentUserToSession();
    }

    // 카카오 회원 탈퇴 및 연동 해제
    @Transactional
    @Override
    public void unlinkAndDeleteKakaoMember(String accessToken, MemberDTO currentUser) {
        try{
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
    
            // 3. 회원 삭제
            deleteMemberAndRelatedData(email);
        }catch (Exception e) {
            System.out.println("카카오 회원 탈퇴 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("카카오 회원 탈퇴 중 오류가 발생했습니다.");
        }
    }

    // 구글 회원 탈퇴 및 연동 해제
    @Transactional
    @Override
    public void unlinkAndDeleteGoogleMember(String accessToken, MemberDTO currentUser) {
        try{
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
    
            // 3. 회원 삭제
            deleteMemberAndRelatedData(email);
        }catch (Exception e) {
            System.out.println("구글 회원 탈퇴 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("구글 회원 탈퇴 중 오류가 발생했습니다.");
        }
    }

    // 매번 `SecurityContextHolder`에서 직접 `OAuth2AuthenticationToken`을 꺼내는 메서드
    private OAuth2AuthenticationToken getOAuth2AuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            return oauthToken;
        }
        return null; // 인증 정보가 없을 경우 null 반환
    }

    // CurrentUserDTO 저장 메서드
    public void saveCurrentUserToSession() {
        // 세션에 사용자 정보 저장
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        MemberDTO user = getCurrentUser(getOAuth2AuthenticationToken());
        // 유저 정보가 없으면 세션에 저장하지 않음
        if (user == null) {
            System.out.println("!!! 유저 정보가 없습니다. 세션에 저장하지 않습니다.");
            return;
        }

        String email = user != null ? user.getEmailId() : null;
        PartnerInfoDTO partnerInfo = partnerInfoService.getPartnerInfo(email); // 사업자번호가 존재하지 않는 경우 null
        // System.out.println("session = " + session);
        // System.out.println("user = " + user);
        // System.out.println("partnerInfo = " + partnerInfo);
        if (user != null){
            CurrentUserDTO currentUser = null;
            String bno = (partnerInfo != null) ? partnerInfo.getBno() : null;
            int unreadAlarm = notificationService.countUnreadNotifications(email, false);

            if (partnerInfo == null) {
                System.out.println("===파트너 정보가 없습니다.===");
            } else {
                System.out.println("===파트너 정보가 있습니다. 사업자번호: " + bno + "===");
            }

            currentUser = CurrentUserDTO.builder()
                    .emailId(user.getEmailId())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .bno(bno)
                    .unreadAlarm(unreadAlarm)
                    .build();

            session.setAttribute("currentUser", currentUser);
        }
    }

    // 회원 DB삭제
    @Transactional
    public void deleteMemberAndRelatedData(String email) {    
        // 사업자 번호 currentUser 세션에서 가져오기
        CurrentUserDTO currentUser = (CurrentUserDTO) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute("currentUser");
        String bno = currentUser != null ? currentUser.getBno() : null;

        try{
            
            // 1. profile Image
            profileService.deleteProfileImage(email); // 프로필 이미지 삭제 메서드 호출
            // 2. notification
            // reviewRepository.deleteByMember_EmailId(email); // 3. reivew (image X) >>> 삭제 고려
            estimateService.deleteByPartnerBno(bno); // 4. estimate 삭제 (payment + review X + image X)
            requestService.allDeleteByEmail(email); // 5. request (hashtag_mapping + image X)
            partnerInfoService.deleteByMemberEmail(email); // 6. partnerInfo 삭제 (payment + image X)
            memberRepository.deleteByEmailId(email); // 7. 마지막에 회원 삭제
        }catch (Exception e) {
            System.out.println("회원 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다.");
        }

        // 세션 파기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession(false); // 현재 세션을 가져오되, 없으면 null 반환
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public void unlinkSocial() {
        // System.out.println("!!!!!!!!!!!!!!!!!!!소셜 로그인 연동 해제 요청!!!!!!!!!!!!!!!!!!!");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String googleToken = (String) session.getAttribute("gogole_token");
    
        System.out.println("googleToken = " + googleToken);

        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            System.out.println("OAuth2AuthenticationToken 인증 정보가 있습니다.");

            String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // 예: "kakao", "google"
            String principalName = auth.getName(); // 사용자 식별자
            OAuth2User oauthUser = oauthToken.getPrincipal(); // 사용자 정보 객체

            // accessToken 꺼내기
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(registrationId, principalName);
            System.out.println("client = " + client);

            if (client == null || client.getAccessToken() == null)return;

            String accessToken = client.getAccessToken().getTokenValue();

            // 이미 revoke 요청했던 토큰이면 중복 호출 방지
            if (!revokedTokens.add(accessToken)) {
                System.out.println("이미 revoke 시도한 토큰입니다. 요청 생략.");
                return;
            }

            if (oauthUser.getAttributes().get("email") != null) { // 구글 로그인인 경우
                WebClient.create()
                        .post()
                        .uri("https://oauth2.googleapis.com/revoke?token=" + accessToken)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } else{ // 카카오 로그인인 경우
                WebClient.create()
                        .post()
                        .uri("https://kapi.kakao.com/v1/user/unlink")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }

        }else if(googleToken != null && !googleToken.isEmpty()) { // 구글 토큰이 있는 경우
            System.out.println("구글 토큰 인증 정보가 있습니다. 토큰 폐기 요청을 보냅니다.");
            WebClient.create()
                    .post()
                    .uri("https://oauth2.googleapis.com/revoke?token=" + googleToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            // 구글 토큰 세션 삭제
            session.removeAttribute("gogole_token");
            System.out.println(">>>구글 토큰 연동 해제 요청 완료.");
        } else {
            // System.out.println(">>>소셜 로그인 연동 해제 요청 실패: 인증 정보가 없습니다.");
        }
    }

    // 이메일로 닉네임 찾는 메서드
    @Override
    public String findNicknameByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일이 누락되었습니다.");
        }
        String nickname = memberRepository.findNicknameByEmailId(email);
        if (nickname == null) {
            throw new IllegalArgumentException("해당 이메일로 가입된 회원이 없습니다.");
        }
        return nickname;
    }
}
