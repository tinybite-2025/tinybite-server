package ita.tinybite.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import ita.tinybite.domain.auth.dto.request.*;
import ita.tinybite.domain.auth.dto.response.AuthResponse;
import ita.tinybite.domain.auth.dto.response.LoginAuthResponse;
import ita.tinybite.domain.auth.dto.response.UserDto;
import ita.tinybite.domain.auth.entity.JwtTokenProvider;
import ita.tinybite.domain.auth.entity.RefreshToken;
import ita.tinybite.domain.auth.kakao.KakaoApiClient;
import ita.tinybite.domain.auth.kakao.KakaoApiClient.KakaoUserInfo;
import ita.tinybite.domain.auth.repository.RefreshTokenRepository;
import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.PlatformType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.AuthErrorCode;
import ita.tinybite.global.exception.errorcode.UserErrorCode;
import ita.tinybite.global.util.NicknameGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoApiClient kakaoApiClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtDecoder appleJwtDecoder;
    private final NicknameGenerator nicknameGenerator;

    @Value("${apple.client-id}")
    private String appleClientId;

    @Value("${google.android-id}")
    private String googleAndroidId;

    @Value("${google.ios-id}")
    private String googleIosId;

    @Transactional
    public AuthResponse kakaoSignup(KakaoSignupRequest request) {
        // 카카오 API로 유저 정보 조회
        KakaoUserInfo kakaoUser = kakaoApiClient.getUserInfo(request.getCode());

        // 이메일 중복 체크
        if (userRepository.findByEmail(kakaoUser.getKakaoAccount().getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .email(kakaoUser.getKakaoAccount().getEmail())
                .nickname(request.getNickname())
                .location(request.getLocation())
                .type(LoginType.KAKAO)
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Refresh Token 저장
        saveRefreshToken(user.getUserId(), refreshToken);

        // 응답 생성
        UserDto userDto = UserDto.from(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userDto)
                .build();
    }

    @Transactional
    public AuthResponse kakaoLogin(KakaoLoginRequest request) {
        // 1. 카카오 API로 유저 정보 조회
        KakaoApiClient.KakaoUserInfo kakaoUser = kakaoApiClient.getUserInfo(request.getAccessToken());

        // 2. DB에서 유저 찾기
        User user = userRepository.findByEmail(kakaoUser.getKakaoAccount().getEmail())
                .orElseThrow(() -> new RuntimeException("가입되지 않은 사용자입니다."));

        // 3. 탈퇴한 사용자 체크
        if (user.getStatus() == UserStatus.WITHDRAW) {
            throw new RuntimeException("탈퇴한 사용자입니다.");
        }

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 5. 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.deleteByUserId(user.getUserId());
        saveRefreshToken(user.getUserId(), refreshToken);

        log.info("로그인 성공 - User ID: {}, Email: {}", user.getUserId(), user.getEmail());

        // 6. 응답 생성
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserDto.from(user))
                .build();
    }

    @Transactional
    public AuthResponse googleSignup(@Valid GoogleAndAppleSignupRequest req) {
        // idToken으로 이메일 추출
        String email = getEmailFromIdToken(req.idToken(), req.platform(), LoginType.GOOGLE);

        // 해당 이메일의 유저 find
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.of(UserErrorCode.USER_NOT_EXISTS));

        // req필드로 유저 필드 업데이트 -> 실질적 회원가입
        user.updateSignupInfo(req, email, LoginType.GOOGLE);
        userRepository.save(user);

        return getAuthResponse(user);
    }

    @Transactional
    public LoginAuthResponse googleLogin(@Valid GoogleAndAppleLoginReq req) {
        // idToken으로 이메일 추출
        String email = getEmailFromIdToken(req.idToken(), req.platformType(), LoginType.GOOGLE);
        // 해당 이메일로 유저 찾은 후 응답 반환 (accessToken, refreshToken)
        return getUser(email, LoginType.GOOGLE);
    }

    @Transactional
    public AuthResponse appleSignup(@Valid GoogleAndAppleSignupRequest req) {
        String email = getEmailFromIdToken(req.idToken(), req.platform(), LoginType.APPLE);

        // 해당 이메일의 유저 find
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.of(UserErrorCode.USER_NOT_EXISTS));

        // req필드로 유저 필드 업데이트 -> 실질적 회원가입
        user.updateSignupInfo(req, email, LoginType.APPLE);
        userRepository.save(user);

        return getAuthResponse(user);
    }

    @Transactional
    public LoginAuthResponse appleLogin(@Valid GoogleAndAppleLoginReq req) {
        // idToken으로 이메일 추출
        String email = getEmailFromIdToken(req.idToken(), req.platformType(), LoginType.APPLE);
        // 해당 이메일로 유저 찾은 후 응답 반환 (AuthResponse)
        return getUser(email, LoginType.APPLE);
    }

    private AuthResponse getAuthResponse(User user) {
        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 5. 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.deleteByUserId(user.getUserId());
        saveRefreshToken(user.getUserId(), refreshToken);

        log.info("로그인 성공 - User ID: {}, Email: {}", user.getUserId(), user.getEmail());

        // 6. 응답 생성
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserDto.from(user))
                .build();
    }

    // 구글과 애플 통합
    private String getEmailFromIdToken(String idToken, PlatformType platformType, LoginType loginType) {
        switch(loginType) {
            case GOOGLE -> {

                String clientId = switch (platformType) {
                    case ANDROID -> googleAndroidId;
                    case IOS -> googleIosId;
                };

                GoogleIdTokenVerifier googleVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                        .setAudience(Collections.singletonList(clientId))
                        .build();

                GoogleIdToken token;
                try {
                    token = googleVerifier.verify(idToken);
                } catch (GeneralSecurityException | IOException e) {
                    throw BusinessException.of(AuthErrorCode.GOOGLE_LOGIN_ERROR);
                }

                if(token == null) {
                    throw BusinessException.of(AuthErrorCode.INVALID_TOKEN);
                }

                return token.getPayload().getEmail();
            }
            case APPLE -> {
                String clientId = appleClientId;
                Jwt jwt = appleJwtDecoder.decode(idToken);

                if(!"https://appleid.apple.com".equals(jwt.getIssuer().toString())) {
                    throw BusinessException.of(AuthErrorCode.INVALID_TOKEN);
                }

                String aud = jwt.getAudience().get(0);
                if (!aud.equals(clientId)) {
                    throw BusinessException.of(AuthErrorCode.INVALID_TOKEN);
                }

                Object emailObject = jwt.getClaims().get("email");
                if(emailObject == null) {
                    throw BusinessException.of(AuthErrorCode.NOT_EXISTS_EMAIL);
                }
                return emailObject.toString();
            }
        }
        return null;
    }

    private LoginAuthResponse getUser(String email, LoginType type) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()) {
            // 이메일로 가입된 유저가 없을 시, INACTIVE로 임시 생성
            // 회원가입 시 해당 임시 유저를 통해 마저 회원가입 진행
            userRepository.save(User.builder()
                    .email(email)
                    .status(UserStatus.INACTIVE)
                    .type(type)
                    .build());
            return LoginAuthResponse.builder().signup(false).build();
        }

        User user = optionalUser.get();

        // 3. 탈퇴한 사용자 체크
        if (user.getStatus() == UserStatus.WITHDRAW) {
            throw new RuntimeException("탈퇴한 사용자입니다.");
        }

        return LoginAuthResponse.builder()
                .signup(true)
                .authResponse(getAuthResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. DB에서 Refresh Token 확인
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 Refresh Token입니다."));

        // 3. User 조회
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 4. 새 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 5. 기존 Refresh Token 삭제 및 새 토큰 저장
        refreshTokenRepository.deleteByToken(refreshTokenValue);
        saveRefreshToken(user.getUserId(), newRefreshToken);

        log.info("토큰 갱신 성공 - User ID: {}", user.getUserId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserDto.from(user))
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.info("로그아웃 - User ID: {}", userId);
    }

    private void saveRefreshToken(Long userId, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public void validateNickname(String nickname) {
        if(userRepository.existsByStatusAndNickname(UserStatus.ACTIVE, nickname))
            throw BusinessException.of(AuthErrorCode.DUPLICATED_NICKNAME);
    }
}