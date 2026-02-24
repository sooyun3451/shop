package com.jpa.market.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa.market.dto.KakaoTokenDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoService {

    public KakaoTokenDto getKakaoAccessToken(String code) {
        String reqUrl = "https://kauth.kakao.com/oauth/token";

        RestTemplate rt = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("grant_type", "authorization_code");
        params.add("client_id", "effe1c548ceac8a20adf8293aafe0a03");
        params.add("client_secret", "YE9wckXBziBGSVf75gIRTC1Z08IuBEci");
        params.add("redirect_uri", "http://localhost:8000/auth/members/kakao");
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, httpHeaders);

        ResponseEntity<String> response = rt.exchange(
                reqUrl,
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
        }catch(JsonProcessingException e) {
            throw new RuntimeException("카카오 토큰 가져오기 실패" + e);
        }

       // return response.getBody();
    }

    public String getKakaoUserInfo(KakaoTokenDto tokenDto) {
        String reqUrl = "https://kapi.kakao.com/v2/user/me";

        RestTemplate rt = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpHeaders.add("Authorization", "Bearer " + tokenDto.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(httpHeaders);

        ResponseEntity<String> response = rt.exchange(
                reqUrl,
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        return response.getBody();
    }


}
