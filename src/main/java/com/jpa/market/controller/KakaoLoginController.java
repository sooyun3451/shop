package com.jpa.market.controller;

import com.jpa.market.dto.KakaoTokenDto;
import com.jpa.market.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("/auth/members/kakao")
    public @ResponseBody String kakaoCallback(@RequestParam("code") String code) {

        KakaoTokenDto accessToken = kakaoService.getKakaoAccessToken(code);

        String userInfo = kakaoService.getKakaoUserInfo(accessToken);

        return "카카오 토큰 정보: " + userInfo;
    }
}
