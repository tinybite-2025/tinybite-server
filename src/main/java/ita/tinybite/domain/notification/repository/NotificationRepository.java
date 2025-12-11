package ita.tinybite.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ita.tinybite.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
