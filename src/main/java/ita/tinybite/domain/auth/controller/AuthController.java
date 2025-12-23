package ita.tinybite.domain.auth.controller;

import ita.tinybite.domain.auth.dto.request.*;
import ita.tinybite.domain.auth.dto.response.AuthResponse;
import ita.tinybite.domain.auth.service.AuthService;
import ita.tinybite.global.response.APIResponse;
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

import static ita.tinybite.global.response.APIResponse.success;

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
                .body(success(response));
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<APIResponse<AuthResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        AuthResponse response = authService.kakaoLogin(request);
        return ResponseEntity.ok(success(response));
    }

    @PostMapping("/google/signup")
    public ResponseEntity<APIResponse<AuthResponse>> googleSignup(
            @Valid @RequestBody GoogleAndAppleSignupRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(success(authService.googleSignup(req)));
    }

    @PostMapping("/google/login")
    public APIResponse<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleAndAppleLoginReq req
    ) {
        return success(authService.googleLogin(req));
    }

    @PostMapping("/apple/signup")
    public ResponseEntity<APIResponse<AuthResponse>> appleSignup(
            @Valid @RequestBody GoogleAndAppleSignupRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(success(authService.appleSignup(req)));
    }

    @PostMapping("/apple/login")
    public APIResponse<AuthResponse> appleLogin(
            @Valid @RequestBody GoogleAndAppleLoginReq req
    ) {
        return success(authService.appleLogin(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(
            @RequestAttribute("userId") Long userId
    ) {
        authService.logout(userId);
        return ResponseEntity.ok(success(null));
    }


    @GetMapping("/nickname/check")
    public APIResponse<?> validateNickname(@RequestParam String nickname) {
        authService.validateNickname(nickname);
        return success();
    }
}

