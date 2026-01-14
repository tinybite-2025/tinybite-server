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
public class TestMessageManager {

	private final NotificationRequestConverter requestConverter;

	public NotificationMulticastRequest createTestRequest(List<String> tokens, String title, String detail) {
		Map<String, String> data = new HashMap<>();
		data.put("eventType", NotificationType.TEST_EVENT.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}
}
