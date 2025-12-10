package ita.tinybite.domain.notification.dto.request;

import java.util.List;
import java.util.Map;

import com.google.firebase.internal.NonNull;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import lombok.Builder;

//@Builder(access = PRIVATE)
@Builder
public record NotificationMulticastRequest(
	@NonNull List<String> tokens,
	String title,
	String body,
	Map<String, String> data
) implements NotificationRequest {

	public static NotificationMulticastRequest of(List<String> tokens, String title, String body, Map<String, String> data) {
		return NotificationMulticastRequest.builder()
			.tokens(tokens)
			.title(title)
			.body(body)
			.data(data)
			.build();
	}

	public MulticastMessage.Builder buildSendMessage() {
		MulticastMessage.Builder builder = MulticastMessage.builder()
			.setNotification(toNotification())
			.addAllTokens(tokens);

		if (this.data != null && !this.data.isEmpty()) {
			builder.putAllData(this.data);
		}

		return builder;
	}

	public Notification toNotification() {
		return Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();
	}

	@Override
	public Map<String, String> data() {
		return this.data;
	}
}
