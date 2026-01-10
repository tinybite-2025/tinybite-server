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
	private static final int MAX_CONTENT_LENGTH = 30;

	private final FcmNotificationSender fcmNotificationSender;
	private final FcmTokenService fcmTokenService;
	private final ChatMessageManager chatMessageManager;
	private final NotificationLogService notificationLogService;
	private final NotificationTransactionHelper notificationTransactionHelper;

	// 1:1 채팅 일반 메시지
	@Transactional
	public void sendOneToOneChatMessage(Long targetUserId, Long chatRoomId, String senderName, String content) {
		String title = senderName;
		String detail = truncateContent(content);
		send(targetUserId, chatRoomId, title, detail, senderName);
	}

	// 1:1 채팅 사진 전송
	@Transactional
	public void sendOneToOneChatImage(Long targetUserId, Long chatRoomId, String senderName) {
		String title = senderName;
		String detail = "📷 사진을 보냈어요";
		send(targetUserId, chatRoomId, title, detail, senderName);
	}

	// 단체 채팅 일반 메시지
	@Transactional
	public void sendGroupChatMessage(Long targetUserId, Long chatRoomId, String partyTitle, String senderName, String content) {
		String title = partyTitle;
		String detail = senderName + ": " + truncateContent(content);
		send(targetUserId, chatRoomId, title, detail, senderName);
	}

	// 단체 채팅 사진 전송
	@Transactional
	public void sendGroupChatImage(Long targetUserId, Long chatRoomId, String partyTitle, String senderName) {
		String title = partyTitle;
		String detail = senderName + ": 📷 사진을 보냈어요";
		send(targetUserId, chatRoomId, title, detail, senderName);
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

	/**
	 * 공통 전송 로직
	 */
	private void send(Long targetUserId, Long chatRoomId, String title, String detail, String senderName) {
		// 알림 로그 저장
		notificationLogService.saveLog(targetUserId, NotificationType.CHAT_NEW_MESSAGE.name(), title, detail);

		// 토큰 조회
		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) return;

		// FCM 요청 생성
		NotificationMulticastRequest request =
			chatMessageManager.createNewChatMessageRequest(tokens, chatRoomId, title, senderName, detail);

		// 발송 및 후처리
		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	/*@Transactional
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
	}*/

	// 텍스트 30자 제한 헬퍼 메서드
	private String truncateContent(String content) {
		if (content == null) return "";
		if (content.length() > MAX_CONTENT_LENGTH) {
			return content.substring(0, MAX_CONTENT_LENGTH) + "...";
		}
		return content;
	}
}
