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

	// 멀티캐스트-대상 유저의 모든 토큰에 전송(새 채팅 메시지)
	public NotificationMulticastRequest createNewChatMessageRequest(
		List<String> tokens, Long chatRoomId, String senderName, String content) {

		Map<String, String> data = new HashMap<>();
		data.put("chatRoomId", String.valueOf(chatRoomId));
		data.put("eventType", NotificationType.CHAT_NEW_MESSAGE.name());
		data.put("senderName", senderName);

		String title = "💬 " + senderName + "님의 새 메시지";
		return requestConverter.toMulticastRequest(tokens, title, content, data);
	}
}
