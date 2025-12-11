package ita.tinybite.domain.notification.entity;

import ita.tinybite.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "fcm_tokens")
public class FcmToken extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "token", nullable = false)
	private String token;

	@Builder.Default
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;

	public void updateToken(String newToken) {
		this.token = newToken;
		this.isActive = Boolean.TRUE;
	}
}
