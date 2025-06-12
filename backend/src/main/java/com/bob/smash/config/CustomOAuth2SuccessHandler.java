package com.bob.smash.config;

import com.bob.smash.entity.Member;
import com.bob.smash.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final String frontServerUrl;

    public CustomOAuth2SuccessHandler(MemberRepository memberRepository, @Value("${front.server.url}") String frontServerUrl) {
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
                Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
                email = (String) kakaoAccount.get("email");  // 필수
                // name = (String) kakaoAccount.get("name");
                phone = (String) kakaoAccount.get("phone_number");  // 선택 정보

                // 주소(배송지) 정보가 있다면 꺼내기
                Object addressObj = kakaoAccount.get("address");
                Map<String, Object> address = null;
                if (addressObj instanceof Map) { // 주소 정보가 Map 형태로 존재하는 경우 (not null)
                    address = (Map<String, Object>) addressObj;
                    region = (String) address.get("address_name");
                }
            }else{ // 구글 로그인인 경우
                email = (String) oauthUser.getAttribute("email");
            }
    
            if(email == null) {// 이메일이 없는 경우, 로그인 실패 처리
                response.sendRedirect(frontServerUrl + "/profile?error=EmailNotFound");
                return;
            }
    
            // 이메일로 회원 조회
            Optional<Member> existMemberWithEmail = memberRepository.findByEmailId(email);
            Optional<Member> existMemberWithTel = Optional.empty();
            if (phone != null) {
                existMemberWithTel = memberRepository.findByTel(phone);
            }
    
            // 만약 회원이 존재하지 않으면 새로 생성
            if (existMemberWithEmail.isEmpty()&&existMemberWithTel.isEmpty()) {
                Member.LoginType loginType;
                // 로그인 타입 설정
                switch (registrationId) {
                    case "kakao" -> loginType = Member.LoginType.kakao;
                    default -> loginType = Member.LoginType.google; // 기본값 설정
                }
    
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
            
        }catch (Exception e) { // 로그인 실패 처리
            System.out.println("OAuth2 로그인 실패: " + e.getMessage());
            e.printStackTrace(); // 로깅

            // 인증 정보 제거
            request.getSession().invalidate(); 
            SecurityContextHolder.clearContext();

            // 실패 페이지 리다이렉트
            response.sendRedirect(frontServerUrl + "/profile?error=SignupFailed");
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
        // 숫자만 남기기
        String onlyNumbers = phone.replaceAll("[^0-9]", "");
        if (onlyNumbers.startsWith("82")) { // 한국 번호로 변환
            onlyNumbers = "0" + onlyNumbers.substring(2);
        }

        return onlyNumbers;
    }
}

