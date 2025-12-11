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

	@Transactional
	public void sendNewChatMessage(
		Long targetUserId,
		Long chatRoomId,
		String senderName,
		String messageContent
	) {
		String title = "💬 " + senderName + "님의 새 메시지";
		notificationLogService.saveLog(targetUserId, NotificationType.CHAT_NEW_MESSAGE.name(), title, messageContent);
		List<String> tokens = getTokens(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			chatMessageManager.createNewChatMessageRequest(tokens, chatRoomId, title, senderName, messageContent);
		notificationSender.send(request);
	}

	@Transactional
	public void sendUnreadReminderNotification(Long targetUserId, Long chatRoomId) {
		String title = "🔔 놓친 메시지가 있어요!";
		String detail = "안 읽은 메시지가 있어요! 지금 확인해 보세요.";
		notificationLogService.saveLog(targetUserId, NotificationType.CHAT_UNREAD_REMINDER.name(), title, detail);

		List<String> tokens = getTokens(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			chatMessageManager.createUnreadReminderRequest(tokens, chatRoomId, title, detail);
		notificationSender.send(request);
	}

	private List<String> getTokens(Long targetUserId) {
		List<String> tokens = fcmTokenService.getTokensByUserId(targetUserId);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 ID: {}에 유효한 FCM 토큰이 없습니다. (푸시 전송 Skip)", targetUserId);
		}
		return tokens;
	}
}
