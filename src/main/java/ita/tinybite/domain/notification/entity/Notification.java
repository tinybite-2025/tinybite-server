package ita.tinybite.domain.notification.entity;

import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false)
	private NotificationType notificationType;

	@Column(name = "notification_detail", columnDefinition = "TEXT")
	private String notificationDetail;

	@Builder.Default
	@Column(name = "is_read", nullable = false)
	private Boolean isRead = Boolean.FALSE;

	public void markAsRead() {
		if (Boolean.FALSE.equals(this.isRead)) {
			this.isRead = Boolean.TRUE;
		}
	}
}
