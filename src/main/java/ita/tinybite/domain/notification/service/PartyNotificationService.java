package ita.tinybite.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.BatchResponse;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.domain.notification.infra.fcm.FcmNotificationSender;
import ita.tinybite.domain.notification.infra.helper.NotificationTransactionHelper;
import ita.tinybite.domain.notification.service.manager.PartyMessageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyNotificationService {

	private final FcmNotificationSender fcmNotificationSender;
	private final FcmTokenService fcmTokenService;
	private final PartyMessageManager partyMessageManager;
	private final NotificationLogService notificationLogService;
	private final NotificationTransactionHelper notificationTransactionHelper;

	//
	@Transactional
	public void sendNewPartyRequestNotification(Long managerId, String requesterNickname, String partyTitle, Long partyId) {
		String title = String.format("🍽️ [%s]님이 참여 요청했어요", requesterNickname);
		String detail = String.format("‘[%s]’ 파티 승인 여부를 확인해 주세요", partyTitle);

		notificationLogService.saveLog(managerId, NotificationType.PARTY_NEW_REQUEST.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(managerId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createNewPartyRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	//
	@Transactional
	public void sendApprovalNotification(Long targetUserId, String partyTitle, Long partyId) {
		// 참여 승인 (파티원에게 전송)
		String title = String.format("🍽️ ‘[%s]’ 파티 승인 완료!", partyTitle);
		String detail = "파티 채팅방에 입장했어요";

		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_APPROVAL.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) return;

		NotificationMulticastRequest request =
			partyMessageManager.createApprovalRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	//
	@Transactional
	public void sendRejectionNotification(Long targetUserId, String partyTitle, Long partyId) {
		// 참여 거절 (파티원에게 전송)
		String title = String.format("🍽️ ‘[%s]’ 😢 참여 거절", partyTitle);
		String detail = "아쉽게도 이번 파티는 함께하지 못해요";

		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_REJECTION.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) return;

		NotificationMulticastRequest request =
			partyMessageManager.createRejectionRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	@Transactional
	public void sendOrderCompleteNotification(List<Long> memberIds, Long partyId) {
		String title = "✅ 상품 주문 완료";
		String detail = "파티장이 상품 주문을 완료했습니다!";
		memberIds.forEach(userId ->
			notificationLogService.saveLog(userId, NotificationType.PARTY_ORDER_COMPLETE.name(), title, detail)
		);

		List<String> tokens = fcmTokenService.getMulticastTokensAndLogIfEmpty(memberIds);
		if (tokens.isEmpty()) {
			return;
		}
		NotificationMulticastRequest request =
			partyMessageManager.createOrderCompleteRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	@Transactional
	public void sendDeliveryReminderNotification(List<Long> memberIds, Long partyId, Long managerId) {

		// 파티 멤버
		String memberTitle = "⏰ 수령 준비 알림";
		String memberDetail = "수령 시간 30분 전입니다! 늦지 않게 준비해주세요.";
		List<Long> commonMembers = memberIds.stream()
			.filter(id -> !id.equals(managerId))
			.toList();

		if (!commonMembers.isEmpty()) {
			commonMembers.forEach(userId ->
				notificationLogService.saveLog(userId, NotificationType.PARTY_DELIVERY_REMINDER.name(), memberTitle, memberDetail)
			);

			List<String> memberTokens = fcmTokenService.getMulticastTokensAndLogIfEmpty(commonMembers);
			if (!memberTokens.isEmpty()) {
				NotificationMulticastRequest memberRequest =
					partyMessageManager.createDeliveryReminderRequest(memberTokens, partyId, memberTitle, memberDetail);

				BatchResponse memberResponse = fcmNotificationSender.send(memberRequest);
				notificationTransactionHelper.handleBatchResponse(memberResponse, memberTokens);
			}
		}

		// 파티장
		String managerTitle = "📍 수령 장소 이동 알림";
		String managerDetail = "수령 시간이 30분 남았습니다. 수령 장소로 이동해주세요!";

		notificationLogService.saveLog(managerId, NotificationType.PARTY_MANAGER_DELIVERY_REMINDER.name(), managerTitle, managerDetail);

		List<String> managerTokens = fcmTokenService.getTokensAndLogIfEmpty(managerId);
		if (!managerTokens.isEmpty()) {
			NotificationMulticastRequest managerRequest =
				partyMessageManager.createManagerDeliveryReminderRequest(managerTokens, partyId, managerTitle, managerDetail);

			BatchResponse managerResponse = fcmNotificationSender.send(managerRequest);
			notificationTransactionHelper.handleBatchResponse(managerResponse, managerTokens);
		}
	}

	//
	@Transactional
	public void sendAutoCloseNotification(List<Long> memberIds, String partyTitle, Long partyId, Long managerId) {
		String title = String.format("🎯 [%s] 인원 모집 완료 !", partyTitle);
		String detail = "파티가 시작 되었어요";

		memberIds.forEach(userId -> {
			notificationLogService.saveLog(userId, NotificationType.PARTY_AUTO_CLOSE.name(), title, detail);
		});

		List<String> tokens = fcmTokenService.getMulticastTokensAndLogIfEmpty(memberIds);
		if (tokens.isEmpty()) return;

		NotificationMulticastRequest request =
			partyMessageManager.createAutoCloseRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	//
	@Transactional
	public void sendPartyCompleteNotification(List<Long> memberIds, String partyTitle, Long partyId) {
		String title = String.format("✅ [%s] 파티 종료", partyTitle);
		String detail = "참여해 주셔서 감사합니다";

		memberIds.forEach(userId ->
			notificationLogService.saveLog(userId, NotificationType.PARTY_COMPLETE.name(), title, detail)
		);

		List<String> tokens = fcmTokenService.getMulticastTokensAndLogIfEmpty(memberIds);
		if (tokens.isEmpty()) return;

		NotificationMulticastRequest request =
			partyMessageManager.createPartyCompleteRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}

	@Transactional
	public void sendMemberLeaveNotification(Long managerId, Long partyId, String leaverName) {
		String title = "⚠️ 파티원 이탈";
		String detail = leaverName + "님이 파티에서 나갔습니다.";

		notificationLogService.saveLog(managerId, NotificationType.PARTY_MEMBER_LEAVE.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(managerId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createMemberLeaveRequest(tokens, partyId, title, detail);

		BatchResponse response = fcmNotificationSender.send(request);
		notificationTransactionHelper.handleBatchResponse(response, tokens);
	}
}
