package ita.tinybite.domain.notification.service.scheduler; // 별도의 scheduler 패키지 권장

import ita.tinybite.domain.notification.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmTokenSchedulerService {

	private final FcmTokenService fcmTokenService;

	/**
	 * 비활성 토큰 정기 삭제 배치 작업.
	 * 매일 새벽 3시 0분에 실행되며, 3개월 이상 접속 기록이 없는 비활성 토큰을 삭제
	 */
	@Scheduled(cron = "0 0 3 * * *")
	public void deleteOldTokensBatch() {
		int deletedCount = fcmTokenService.deleteOldInactiveTokens(ChronoUnit.MONTHS, 3);
		log.info("오래된 FCM 토큰 정기 삭제 배치 완료. 삭제 건수: {}", deletedCount);
	}
}