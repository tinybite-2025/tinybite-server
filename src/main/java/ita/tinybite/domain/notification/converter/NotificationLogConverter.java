package ita.tinybite.domain.notification.converter;

import org.springframework.stereotype.Component;

import ita.tinybite.domain.notification.entity.Notification;
import ita.tinybite.domain.notification.enums.NotificationType;

@Component
public class NotificationLogConverter {

	public Notification toEntity(Long targetUserId, String type, String detail) {

		NotificationType notificationType = NotificationType.valueOf(type);

		return Notification.builder()
			.userId(targetUserId)
			.notificationType(notificationType)
			.notificationDetail(detail)
			.isRead(false)
			.build();
	}
}
