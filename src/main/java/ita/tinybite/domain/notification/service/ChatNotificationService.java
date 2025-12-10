package ita.tinybite.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.domain.notification.service.manager.ChatMessageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatNotificationService {

	private final NotificationSender notificationSender;
	private final FcmTokenService fcmTokenService;
	private final ChatMessageManager chatMessageManager;
	private final NotificationLogService notificationLogService;

	// 새 채팅 메시지 알림 전송
	@Transactional
	public void sendNewChatMessage(
		Long targetUserId,
		Long chatRoomId,
		String senderName,
		String messageContent
	) {
		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_APPROVAL.name(), messageContent);

		List<String> tokens = fcmTokenService.getTokensByUserId(targetUserId);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 ID: {}에 유효한 FCM 토큰이 없습니다.", targetUserId);
			return;
		}

		NotificationMulticastRequest request =
			chatMessageManager.createNewChatMessageRequest(tokens, chatRoomId, senderName, messageContent);

		notificationSender.send(request);
	}

}
