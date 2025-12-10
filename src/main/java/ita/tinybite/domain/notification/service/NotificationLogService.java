package ita.tinybite.domain.notification.service;

import org.springframework.stereotype.Service;

import ita.tinybite.domain.notification.converter.NotificationLogConverter;
import ita.tinybite.domain.notification.entity.Notification;
import ita.tinybite.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogService {
	private final NotificationRepository notificationRepository;
	private final NotificationLogConverter notificationLogConverter;

	public void saveLog(Long targetUserId, String type, String detail) {
		Notification notification = notificationLogConverter.toEntity(targetUserId, type, detail);
		notificationRepository.save(notification);
	}
}