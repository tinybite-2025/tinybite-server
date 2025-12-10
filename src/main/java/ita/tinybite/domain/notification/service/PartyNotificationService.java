package ita.tinybite.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.domain.notification.service.manager.PartyMessageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyNotificationService {

	private final NotificationSender notificationSender;
	private final FcmTokenService fcmTokenService;
	private final PartyMessageManager partyMessageManager;
	private final NotificationLogService notificationLogService;

	// 파티 참여 승인 알림 전송
	@Transactional
	public void sendApprovalNotification(Long targetUserId, Long partyId) {

		String detail = "파티 참여가 승인되었습니다! 지금 확인하세요.";
		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_APPROVAL.name(), detail);

		List<String> tokens = fcmTokenService.getTokensByUserId(targetUserId);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 ID: {}에 유효한 FCM 토큰이 없습니다.", targetUserId);
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createApprovalRequest(tokens, partyId, detail);

		notificationSender.send(request);

		// BatchResponse를 받아 실패 토큰을 비활성화하는 후처리 로직을 여기에 추가
	}
}
