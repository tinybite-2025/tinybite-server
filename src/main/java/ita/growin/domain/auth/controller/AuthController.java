package ita.growin.domain.auth.controller;

import ita.growin.domain.auth.dto.request.KakaoLoginRequest;
import ita.growin.domain.auth.dto.request.KakaoSignupRequest;
import ita.growin.domain.auth.dto.request.RefreshTokenRequest;
import ita.growin.domain.auth.dto.response.AuthResponse;
import ita.growin.domain.auth.service.AuthService;
import ita.growin.global.response.APIResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final AuthService authService;

    @GetMapping("/kakao")
    public ResponseEntity<Map<String, String>> kakaoLogin() throws IOException {
        String kakaoAuthUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "profile_nickname,account_email")
                .build()
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("url", kakaoAuthUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao/signup")
    public ResponseEntity<APIResponse<AuthResponse>> kakaoSignup(
            @Valid @RequestBody KakaoSignupRequest request
    ) {
        AuthResponse response = authService.kakaoSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success(response));
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<APIResponse<AuthResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        AuthResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(APIResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(APIResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(
            @RequestAttribute("userId") Long userId
    ) {
        authService.logout(userId);
        return ResponseEntity.ok(APIResponse.success(null));
    }
}

