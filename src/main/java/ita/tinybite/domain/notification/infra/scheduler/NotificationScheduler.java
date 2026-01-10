package ita.tinybite.domain.notification.infra.scheduler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ita.tinybite.domain.notification.service.facade.NotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final NotificationFacade notificationFacade;

	@Scheduled(cron = "0 * * * * *") // 1분마다
	public void processPendingApprovalReminders() {
		Set<String> keys = redisTemplate.keys("pending_reminder:*");
		if (keys.isEmpty()) return;

		LocalDateTime now = LocalDateTime.now();

		for (String key : keys) {
			try {
				// Redis에서 JSON 문자열 가져오기
				String jsonValue = redisTemplate.opsForValue().get(key);
				if (jsonValue == null) continue;

				Map<String, Object> data = objectMapper.readValue(jsonValue, Map.class);

				Long hostId = Long.valueOf(data.get("hostId").toString());
				Long partyId = Long.valueOf(data.get("partyId").toString());
				Long requesterId = Long.valueOf(data.get("requesterId").toString());
				String requesterNickname = (String) data.get("requesterNickname");
				int retryCount = (int) data.get("retryCount");
				LocalDateTime lastSentAt = LocalDateTime.parse((String) data.get("lastSentAt"));

				// 10분 확인
				if (now.isAfter(lastSentAt.plusMinutes(10))) {

					if (retryCount >= 3) {
						log.info("리마인드 3회 초과로 자동 삭제: {}", key);
						redisTemplate.delete(key);
						continue;
					}

					notificationFacade.notifyPendingApprovalReminder(
						hostId,
						partyId,
						requesterId,
						requesterNickname
					);

					data.put("retryCount", retryCount + 1);
					data.put("lastSentAt", now.toString());

					redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data));

					log.info("리마인드 알림 발송 및 데이터 갱신 완료: {}회차", retryCount + 1);
				}
			} catch (Exception e) {
				log.error("리마인드 스케줄러 처리 중 에러 발생 (Key: {}): {}", key, e.getMessage());
			}
		}
	}
}