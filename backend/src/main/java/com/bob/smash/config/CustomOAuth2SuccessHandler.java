package com.bob.smash.config;

import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference; // 타입검사(안정성)


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MemberRepository memberRepository;
    private final String frontServerUrl;
    
    public CustomOAuth2SuccessHandler(
        MemberRepository memberRepository, 
        @Value("${front.server.url}") String frontServerUrl,
        OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.authorizedClientService = authorizedClientService;
        this.memberRepository = memberRepository;
        this.frontServerUrl = frontServerUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal(); // OAuth2User 객체를 통해 사용자 정보에 접근

        System.out.println("@@@ OAuth2 로그인 성공: " + oauthUser.getAttributes());

        // OAuth2User에서 필요한 정보 추출
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // "google" or "kakao"
        String email = null;
        // String name = null;
        String phone = null;
        String region = null;

        try{
            if("kakao".equals(registrationId)) { // 카카오 로그인인 경우
                System.out.println("Kakao 로그인 처리 중...");

                Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
                email = (String) kakaoAccount.get("email");  // 필수
                phone = (String) kakaoAccount.get("phone_number");  // 선택 정보

                // access token 가져오기
                OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
                OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                        authToken.getAuthorizedClientRegistrationId(),
                        authToken.getName()
                );

                String accessToken = authorizedClient.getAccessToken().getTokenValue();

                // 배송지 API 호출
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                HttpEntity<?> entity = new HttpEntity<>(headers);

                try {
                    ResponseEntity<Map<String, Object>> adressResponse = restTemplate.exchange(
                        "https://kapi.kakao.com/v1/user/shipping_address",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

                    Map<String, Object> responseBody = adressResponse.getBody();
                    // System.out.println("배송지 응답 =================== " + responseBody);

                    List<Map<String, Object>> addresses = (List<Map<String, Object>>) responseBody.get("shipping_addresses");

                    if (addresses != null && !addresses.isEmpty()) {
                        Map<String, Object> firstAddress = addresses.get(0);
                        String baseAddress = (String) firstAddress.get("base_address");
                        String detailAddress = (String) firstAddress.get("detail_address");

                        String fullAddress = (baseAddress != null ? baseAddress : "") 
                                           + (detailAddress != null && !detailAddress.isEmpty() ? " " + detailAddress : "");
                        region = fullAddress;
                    }

                    System.out.println("배송지 정보 =================== " + region);

                } catch (Exception e) {
                    System.out.println("배송지 정보 가져오기 실패: " + e.getMessage());
                }
            }else{ // 구글 로그인인 경우
                System.out.println("Google 로그인 처리 중...");
                email = (String) oauthUser.getAttribute("email");
            }
    
            if(email == null) {// 이메일이 없는 경우, 로그인 실패 처리
                response.sendRedirect(frontServerUrl + "/profile?error=EmailNotFound");
                return;
            }
    
            // 회원 조회
            Optional<Member> existMemberWithEmail = memberRepository.findByEmailId(email);
            Optional<Member> existMemberWithTel = memberRepository.findByTel(formatPhoneNumber(phone));
            Member existingMember = existMemberWithEmail.orElse(existMemberWithTel.orElse(null));

            // DB에 저장된 전화번호 우선 사용
            if (existingMember != null) {
                phone = existingMember.getTel();
            }

            // 전화번호가 없으면 프론트에서 번호 입력 페이지로 보내기 / DB에 이메일이 존재할 경우 그냥 return
            if (phone == null || phone.isBlank()) {
                request.getSession().setAttribute("social_email", email);
                request.getSession().setAttribute("social_provider", registrationId);
                request.getSession().setAttribute("nickname", generateUniqueNickname());

                if (existingMember != null) {
                    System.out.println("===전화번호 X 이메일 O >>> 기존 회원 >>> 핸드폰 입력 페이지===");
                } else {
                    System.out.println("===전화번호 X 이메일 X >>> 신규 회원 >>> 핸드폰 입력 페이지===");
                }

                response.sendRedirect(frontServerUrl + "/member/authenticated");
                return;
            }
            
            Member.LoginType loginType;
            // 로그인 타입 설정
            switch (registrationId) {
                case "kakao" -> loginType = Member.LoginType.kakao;
                default -> loginType = Member.LoginType.google; // 기본값 설정
            }

            // loginType 판단 후 비교
            if (existingMember != null) {
                if (isSameLoginType(existingMember, loginType)) { // 정상 로그인
                    System.out.println("===이미 존재하는 동일 회원===");
                    response.sendRedirect(frontServerUrl + "/profile");
                    return;
                } else { // 다른 계정으로 로그인 요청
                    System.out.println("===이미 존재하는 다른계정 회원===");
                    response.sendRedirect(frontServerUrl + "/profile?error=" + existingMember.getLoginType() + "AlreadyExists");
                    return;
                }
            }
    
            // 만약 회원이 존재하지 않으면 새로 생성
            if (existMemberWithEmail.isEmpty() && existMemberWithTel.isEmpty()) { // 중복되는 이메일이나 전화번호가 없을 때만 회원 생성
                System.out.println("===새로운 회원 생성===");
    
                Member newMember = Member.builder()
                    .emailId(email)
                    // .name(oauthUser.getAttribute("name"))
                    .nickname(generateUniqueNickname())  // 유니크 닉네임
                    .createdAt(LocalDateTime.now())
                    .loginType(loginType)
                    .role((byte) 0) // 기본 역할 설정 (0: 일반 사용자)
                    .tel(formatPhoneNumber(phone))
                    .region(region)
                    .build();
    
                memberRepository.save(newMember);
            }
    
            // 로그인 성공 후 원하는 페이지로 리다이렉트
            response.sendRedirect(frontServerUrl + "/profile"); // 프로필 페이지
            return;
            
        }catch (Exception e) { // 로그인 실패 처리
            System.out.println("OAuth2 로그인 실패: " + e.getMessage());
            e.printStackTrace(); // 로깅

            // 인증 정보 제거
            request.getSession().invalidate(); 
            SecurityContextHolder.clearContext();

            // 실패 페이지 리다이렉트
            response.sendRedirect(frontServerUrl + "/profile?error=SignupFailed");
            return;
        }
    }

    // 닉네임 생성 메서드
    /**
     * 유니크한 닉네임을 생성하는 메서드
     * 닉네임은 형식: "수줍은 너구리#7f3"과 같이 생성
     * 중복된 닉네임이 있을 경우 최대 10회 시도 후 실패하면 null 반환
     * 총 조합: 50 × 50 × 4096 = 10,240,000가지
     */
    private String generateUniqueNickname() {
        String[] adjectives = {
            "수줍은", "화난", "행복한", "용감한", "귀여운",
            "슬픈", "엉뚱한", "멋진", "날쌘", "재빠른",
            "조용한", "상냥한", "우울한", "활발한", "똑똑한",
            "느긋한", "쾌활한", "차가운", "따뜻한", "열정적인",
            "게으른", "배고픈", "졸린", "잠이많은", "반짝이는",
            "깜찍한", "웃긴", "용의주도한", "신중한", "자신감있는",
            "창의적인", "명랑한", "지적인", "독특한", "신나는",
            "자유로운", "사려깊은", "친절한", "겁쟁이", "냉정한",
            "재미있는", "엉뚱한", "귀찮은", "호기심많은", "엉성한",
            "섬세한", "털털한", "도도한", "수상한", "믿음직한"
        };

        String[] animals = {
            "너구리", "사자", "고양이", "호랑이", "강아지",
            "토끼", "곰", "다람쥐", "여우", "판다",
            "고래", "수달", "늑대", "하마", "기린",
            "코끼리", "캥거루", "치타", "펭귄", "부엉이",
            "올빼미", "두더지", "햄스터", "삵", "이구아나",
            "돌고래", "라마", "퓨마", "도마뱀", "스컹크",
            "고슴도치", "하이에나", "표범", "까치", "비버",
            "청설모", "앵무새", "앵무", "살쾡이", "바다표범",
            "물개", "오소리", "알파카", "재규어", "홍학",
            "백조", "카멜레온", "여치", "두루미", "노루"
        };
    
        String nickname;
        int attempt = 0;
    
        do {
            String adjective = adjectives[(int) (Math.random() * adjectives.length)];
            String animal = animals[(int) (Math.random() * animals.length)];
            String suffix = Integer.toHexString((int) (Math.random() * 0xFFF)); // 예: "7f3"
    
            nickname = adjective + " " + animal + "#" + suffix;
            attempt++;
        } while (memberRepository.existsByNickname(nickname) && attempt < 10); // 최대 10회 시도
    
        return nickname;
    }

    // 번호 가공 메서드 (+부터 공백까지 제거)
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null; // 전화번호가 없으면 null 반환
        }
        // 숫자만 남기기
        String onlyNumbers = phone.replaceAll("[^0-9]", "");
        if (onlyNumbers.startsWith("82")) { // 한국 번호로 변환
            onlyNumbers = "0" + onlyNumbers.substring(2);
        }
        return onlyNumbers;
    }

    // 현재 로그인 타입과 비교
    private boolean isSameLoginType(Member member, Member.LoginType loginType) {
        return member != null && member.getLoginType() == loginType;
    }
}

