package ita.tinybite.domain.notification.service;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.dto.request.NotificationSingleRequest;
import ita.tinybite.domain.notification.service.creator.APNsConfigCreator;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.FcmErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//토큰 목록과 Message 객체를 받아 FCM 서버로 전송
// BatchResponse를 받아 실패 토큰을 비활성화하는 후처리 로직 추가
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSender {

	private final FirebaseMessaging firebaseMessaging;

	private static final int MULTICAST_TOKEN_LIMIT = 500;

	public String send(final NotificationSingleRequest request) {
		try {
			Message message = request.buildMessage()
				.setApnsConfig(APNsConfigCreator.createDefaultConfig())
				.build();

			String response = firebaseMessaging.send(message);
			log.info("단일 알림 전송 성공 (토큰: {}): {}", request.token(), response);
			return response;

		} catch (FirebaseMessagingException e) {
			log.error("FCM 단일 전송 실패 (토큰: {}): {}", request.token(), e.getMessage(), e);
			throw new BusinessException(FcmErrorCode.CANNOT_SEND_NOTIFICATION);
		}
	}

	public BatchResponse send(final NotificationMulticastRequest request) {
		if (request.tokens().size() > MULTICAST_TOKEN_LIMIT) {
			log.warn("멀티캐스트 실패: 토큰 {}개 (500개 제한 초과)", request.tokens().size());
			throw new BusinessException(FcmErrorCode.FCM_TOKEN_LIMIT_EXCEEDED);
		}
		try {
			MulticastMessage message = request.buildSendMessage()
				.setApnsConfig(APNsConfigCreator.createDefaultConfig())
				.build();

			BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
			log.info("멀티캐스트 전송 완료. 성공: {}, 실패: {}",
				response.getSuccessCount(), response.getFailureCount());
			return response;
		} catch (FirebaseMessagingException e) {
			log.error("FCM 멀티캐스트 전송 중 FCM 서버 오류 발생", e);
			throw new BusinessException(FcmErrorCode.CANNOT_SEND_NOTIFICATION);
		}
	}
}