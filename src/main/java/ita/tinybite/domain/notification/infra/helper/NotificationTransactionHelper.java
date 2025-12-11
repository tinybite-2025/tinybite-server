package ita.tinybite.domain.notification.infra.helper;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import ita.tinybite.domain.notification.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTransactionHelper {

	private final FcmTokenService fcmTokenService;

	@Transactional(propagation = Propagation.REQUIRES_NEW) // 메인 트랜잭션에 영향을 주지 않도록 분리
	public void handleBatchResponse(BatchResponse response, List<String> allTokens) {
		if (response.getFailureCount() > 0) {
			List<String> failedTokens = extractUnregisteredTokens(response, allTokens);
			if (!failedTokens.isEmpty()) {
				fcmTokenService.deactivateTokens(failedTokens);
				log.warn("FCM 응답 기반 토큰 {}건 비활성화 완료. 실패 건수: {}", failedTokens.size(), response.getFailureCount());
			}
		}
	}

	private List<String> extractUnregisteredTokens(BatchResponse response, List<String> allTokens) {
		List<String> unregisteredTokens = new ArrayList<>();

		for (int i = 0; i < response.getResponses().size(); i++) {
			SendResponse sendResponse = response.getResponses().get(i);

			if (!sendResponse.isSuccessful()) {
				var exception = sendResponse.getException();

				if (exception != null) {
					var errorCode = exception.getMessagingErrorCode();

					if ((errorCode == MessagingErrorCode.UNREGISTERED
						|| errorCode == MessagingErrorCode.INVALID_ARGUMENT)) {
							unregisteredTokens.add(allTokens.get(i));
						}
				}
			}
		}
		return unregisteredTokens;
	}
}