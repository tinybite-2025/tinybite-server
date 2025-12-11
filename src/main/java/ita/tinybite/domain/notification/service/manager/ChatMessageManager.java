package ita.tinybite.domain.notification.service.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ita.tinybite.domain.notification.converter.NotificationRequestConverter;
import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessageManager {

	private final NotificationRequestConverter requestConverter;
	private static final String KEY_CHAT_ROOM_ID = "chatRoomId";
	private static final String KEY_EVENT_TYPE = "eventType";
	private static final String KEY_SENDER_NAME = "senderName";

	public NotificationMulticastRequest createNewChatMessageRequest(
		List<String> tokens, Long chatRoomId, String title, String senderName, String content) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_CHAT_ROOM_ID, String.valueOf(chatRoomId));
		data.put(KEY_EVENT_TYPE, NotificationType.CHAT_NEW_MESSAGE.name());
		data.put(KEY_SENDER_NAME, senderName);

		return requestConverter.toMulticastRequest(tokens, title, content, data);
	}

	public NotificationMulticastRequest createUnreadReminderRequest(
		List<String> tokens, Long chatRoomId, String title, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_CHAT_ROOM_ID, String.valueOf(chatRoomId));
		data.put(KEY_EVENT_TYPE, NotificationType.CHAT_UNREAD_REMINDER.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}
}
