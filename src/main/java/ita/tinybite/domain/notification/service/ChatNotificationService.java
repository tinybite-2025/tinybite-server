package ita.tinybite.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.BatchResponse;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.domain.notification.infra.fcm.FcmNotificationSender;
import ita.tinybite.domain.notification.infra.helper.NotificationTransactionHelper;
import ita.tinybite.domain.notification.service.manager.ChatMessageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatNotificationService {

	private final FcmNotificationSender fcmNotificationSender;
	private final FcmTokenService fcmTokenService;
	private final ChatMessageManager chatMessageManager;
	private final NotificationLogService notificationLogService;
	private final NotificationTransactionHelper notificationTransactionHelper;

	@Transactional
	public void sendNewChatMessage(
		Long targetUserId,
		Long chatRoomId,
		String senderName,
		String messageContent
	) {
		String title = "💬 " + senderName + "님의 새 메시지";
		notificationLogService.saveLog(targetUserId, NotificationType.CHAT_NEW_MESSAGE.name(), title, messageContent);

		// 추후 구현 필요 사항: 뱃지 카운트
		// APNs 뱃지 카운트를 동적으로 설정?
		// 안 읽은 메시지 알림 반환 방식 정의 필요
		// ChatService를 통해 해당 senderName을 통해 총 안 읽은 메시지 주입받아 이를 통해 뱃지 카운트 형성
		// 현재는 뱃지 카운트 인자 없이 단일 알림 여러개 전송 구조

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			chatMessageManager.createNewChatMessageRequest(tokens, chatRoomId, title, senderName, messageContent);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	@Transactional
	public void sendUnreadReminderNotification(Long targetUserId, Long chatRoomId) {
		String title = "🔔 놓친 메시지가 있어요!";
		String detail = "안 읽은 메시지가 있어요! 지금 확인해 보세요.";
		notificationLogService.saveLog(targetUserId, NotificationType.CHAT_UNREAD_REMINDER.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			chatMessageManager.createUnreadReminderRequest(tokens, chatRoomId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}
}
