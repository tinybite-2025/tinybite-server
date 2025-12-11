package ita.tinybite.domain.notification.dto.request;

import java.util.Map;

import com.google.firebase.messaging.Notification;

public interface NotificationRequest {
	String title();
	String body();
	Notification toNotification();
	Map<String, String> data();
}
