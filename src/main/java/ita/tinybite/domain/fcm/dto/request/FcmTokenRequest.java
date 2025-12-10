package ita.tinybite.domain.fcm.dto.request;

import jakarta.validation.constraints.NotNull;

public record FcmTokenRequest(
	@NotNull(message = "FCM 토큰은 필수입니다.")
	String token
) {}
