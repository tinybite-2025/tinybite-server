package ita.tinybite.domain.auth.service;

import ita.tinybite.domain.auth.dto.request.KakaoLoginRequest;
import ita.tinybite.domain.auth.dto.request.KakaoSignupRequest;
import ita.tinybite.domain.auth.dto.request.RefreshTokenRequest;
import ita.tinybite.domain.auth.dto.response.AuthResponse;
import ita.tinybite.domain.auth.dto.response.UserDto;
import ita.tinybite.domain.auth.entity.JwtTokenProvider;
import ita.tinybite.domain.auth.entity.RefreshToken;
import ita.tinybite.domain.auth.kakao.KakaoApiClient;
import ita.tinybite.domain.auth.kakao.KakaoApiClient.KakaoUserInfo;
import ita.tinybite.domain.auth.repository.RefreshTokenRepository;
import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.util.NicknameGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoApiClient kakaoApiClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final NicknameGenerator nicknameGenerator;

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
}