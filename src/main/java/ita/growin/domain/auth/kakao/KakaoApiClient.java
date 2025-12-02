package ita.growin.domain.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import ita.growin.domain.auth.dto.response.KakaoAuthToken;
import ita.growin.domain.auth.dto.response.KakaoTokenResponse;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KakaoApiClient {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;
    private final RestTemplate restTemplate;
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_AUTH_CODE_URL ="https://kauth.kakao.com/oauth/authorize";

    public KakaoApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public KakaoUserInfo getUserInfo(String code) {
        try {
//            ResponseEntity<KakaoAuthToken> authorizeCode =requestAuthorizeCode(code);
            KakaoTokenResponse tokenResponse = requestKakaoAccessToken(code);
            ResponseEntity<KakaoUserInfo> kakaoUserInfo = requestUserInfo(tokenResponse.getAccess_token());

            return kakaoUserInfo.getBody();

        } catch (Exception e) {
            log.error("카카오 API 호출 실패", e);
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }

    private ResponseEntity<KakaoUserInfo> requestUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                KakaoUserInfo.class
        );

        return response;

    }

    private KakaoTokenResponse requestKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);
        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);

        ResponseEntity<KakaoTokenResponse> res = restTemplate.postForEntity(
                KAKAO_TOKEN_URL, req, KakaoTokenResponse.class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("카카오 토큰 발급 실패: " + res.getStatusCode());
        }
        return res.getBody();
    }

    private ResponseEntity<KakaoAuthToken> requestAuthorizeCode(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
        return restTemplate.postForEntity(KAKAO_AUTH_CODE_URL, req, KakaoAuthToken.class);
    }

    @Getter
    @Setter
    public static class KakaoUserInfo {
        private Long id;

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;

        @Getter
        @Setter
        public static class KakaoAccount {
            private String email;

            @JsonProperty("email_needs_agreement")
            private Boolean emailNeedsAgreement;

            private Profile profile;

            @Getter
            @Setter
            public static class Profile {
                private String nickname;
            }
        }
    }
}
