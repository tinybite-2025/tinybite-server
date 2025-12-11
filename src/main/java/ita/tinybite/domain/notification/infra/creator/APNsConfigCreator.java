package ita.tinybite.domain.notification.infra.creator;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class APNsConfigCreator {

	public static ApnsConfig createDefaultConfig() {
		return ApnsConfig.builder()
			.setAps(Aps.builder()
				.setSound("default")
				.setBadge(1)
				.build()
			)
			.build();
	}

	// 이벤트별로 동적인 뱃지 숫자 설정
	public static ApnsConfig createConfigWithBadge(int badgeCount) {
		return ApnsConfig.builder()
			.setAps(Aps.builder()
				.setSound("default")
				.setBadge(badgeCount)
				.build()
			)
			.build();
	}
}
