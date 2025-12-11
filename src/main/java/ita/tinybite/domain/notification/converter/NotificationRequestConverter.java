package ita.tinybite.domain.notification.converter;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;

@Component
public class NotificationRequestConverter {

	public NotificationMulticastRequest toMulticastRequest(
		List<String> tokens,
		String title,
		String body,
		Map<String, String> data) {

		return NotificationMulticastRequest.builder()
			.tokens(tokens)
			.title(title)
			.body(body)
			.data(data)
			.build();
	}
}
