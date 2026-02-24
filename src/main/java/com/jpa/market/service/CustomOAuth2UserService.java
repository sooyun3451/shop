package com.jpa.market.service;

import com.jpa.market.constant.OAuthType;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

// DefaultOAuth2UserService: 소셜 서버에 가서 엑세스 토큰을 이용하여 사용자의 정보를 가져옴
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // OAuth2 서버에 사용자 정보를 요청
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // OAuth2 서버에서 내려준 사용자의 정보를 Map형태로 받음
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오인지, 네이버인지 확인(어떤 소셜 로그인인지 확인)
        String registId = userRequest.getClientRegistration().getRegistrationId();

        // 사용자의 정보를 담아오기 위한 변수 선언
        String providerId = ""; // 소셜에서 발급한 고유 ID
        String nickName = ""; // 이름 또는 닉네임
        String email = ""; // 이메일
        OAuthType oAuthType = null; // 소셜 타입
        String nameAttributeKey = ""; // OAuth2User에서 사용할 키

        // 접속한 소셜이 카카오면...
        if("kakao".equals(registId)) {
            // 카카오 고유의 id값을 저장
            providerId = attributes.get("id").toString();


            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String)kakaoAccount.get("email");
            nickName = (String) profile.get("nickname");
            oAuthType = OAuthType.KAKAO;
            nameAttributeKey = "id";

        }else if("naver".equals(registId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");

            providerId = (String)response.get("id");
            email = (String)response.get("email");
            nickName = (String) response.get("name");
            oAuthType = OAuthType.NAVER;
            nameAttributeKey = "response";

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다.");
        }

        // 소셜타입_서셜Id를 조함해서 로그인 id를 생성(KAKAO_123 NAVER_345)
        String loginId = oAuthType.name() + "_" + providerId;

        // 람다식 안에서 사용하는 지역변수는 final이거나 값을 한번만 대입하고 절대 변경되지 않는 변수여야 함.
        final String finalNickName = nickName;
        final String finalEmail = email;
        final OAuthType finalOAuthType = oAuthType;

        // DB에 회원이 있으면 조회, 없으면 신규 등록
        Member member = memberRepository.findByLoginId(loginId).orElseGet(() -> {
            // 회원의 임시 비밀번호를 생성
            String encodePwd = passwordEncoder.encode("OAUTH_" + UUID.randomUUID());
            // Member엔티티에 만든 정적 메서드를 호출
            Member newMember = Member.createOAuthMember(loginId, finalNickName, finalEmail, encodePwd, finalOAuthType);
            return memberRepository.save(newMember);
        });

        // 시큐리티가 인증 객체로 사용할 OAuthUser 객체를 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes,
                nameAttributeKey
        );
    }
}
