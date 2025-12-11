package ita.tinybite.domain.notification.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.entity.FcmToken;
import ita.tinybite.domain.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FcmTokenService {
	private final FcmTokenRepository fcmTokenRepository;

	@Transactional
	public void saveOrUpdateToken(Long userId, String token) {
		Optional<FcmToken> existingToken = fcmTokenRepository.findByUserIdAndToken(userId, token);

		if (existingToken.isPresent()) {
			FcmToken fcmToken = existingToken.get();
			if (!Boolean.TRUE.equals(fcmToken.getIsActive())) {
				fcmToken.updateToken(token);
			}
		} else {
			FcmToken newToken = FcmToken.builder()
				.userId(userId)
				.token(token)
				.build();
			fcmTokenRepository.save(newToken);
		}
	}

	public List<String> getTokensByUserId(Long userId) {
		return fcmTokenRepository.findAllByUserIdAndIsActiveTrue(userId).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
	}

	public List<String> getTokensByUserIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return List.of();
		}
		return fcmTokenRepository.findAllByUserIdInAndIsActiveTrue(userIds).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
	}

	// 단일 사용자 토큰 조회
	public List<String> getTokensAndLogIfEmpty(Long targetUserId) { // (이름 변경)
		List<String> tokens = getTokensByUserId(targetUserId);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 ID: {}에 유효한 FCM 토큰이 없습니다. (푸시 Skip)", targetUserId);
		}
		return tokens;
	}

	// 다중 사용자 토큰 조회
	public List<String> getMulticastTokensAndLogIfEmpty(List<Long> userIds) { // (이름 변경)
		List<String> tokens = getTokensByUserIds(userIds);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 목록(IDs: {})에 유효한 FCM 토큰이 없습니다. (푸시 Skip)", userIds);
		}
		return tokens;
	}
}
