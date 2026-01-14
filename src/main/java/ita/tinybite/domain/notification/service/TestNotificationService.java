package ita.tinybite.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.BatchResponse;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.domain.notification.infra.fcm.FcmNotificationSender;
import ita.tinybite.domain.notification.infra.helper.NotificationTransactionHelper;
import ita.tinybite.domain.notification.service.manager.TestMessageManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestNotificationService {
	private final FcmNotificationSender fcmNotificationSender;
	private final FcmTokenService fcmTokenService;
	private final TestMessageManager testMessageManager;
	private final NotificationLogService notificationLogService;
	private final NotificationTransactionHelper notificationTransactionHelper;

	@Transactional
	public void sendTestNotification(Long targetUserId) {
		String title = "알림 테스트 제목";
		String detail = "알림 테스트 내용";

		notificationLogService.saveLog(targetUserId, NotificationType.TEST_EVENT.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty())
			return;

		NotificationMulticastRequest request = testMessageManager.createTestRequest(tokens, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}
}