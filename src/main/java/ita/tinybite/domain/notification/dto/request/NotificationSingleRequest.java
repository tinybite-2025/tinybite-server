package ita.tinybite.domain.notification.dto.request;

import java.util.Map;

import com.google.firebase.internal.NonNull;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.Builder;

@Builder
public record NotificationSingleRequest(
	@NonNull String token,
	String title,
	String body,
	Map<String, String> data
) implements NotificationRequest {

	public Message.Builder buildMessage() {
		Message.Builder builder = Message.builder()
			.setToken(token)
			.setNotification(toNotification());

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
