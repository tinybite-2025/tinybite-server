package ita.tinybite.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import ita.tinybite.domain.notification.service.manager.PartyMessageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyNotificationService {

	private final NotificationSender notificationSender;
	private final FcmTokenService fcmTokenService;
	private final PartyMessageManager partyMessageManager;
	private final NotificationLogService notificationLogService;

	@Transactional
	public void sendApprovalNotification(Long targetUserId, Long partyId) {
		String title = "🎉 파티 참여 승인";
		String detail = "파티 참여가 승인되었습니다! 지금 확인하세요.";
		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_APPROVAL.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}
		NotificationMulticastRequest request =
			partyMessageManager.createApprovalRequest(tokens, partyId, title, detail);
		notificationSender.send(request);
	}

	@Transactional
	public void sendRejectionNotification(Long targetUserId, Long partyId) {
		String title = "🚨 파티 참여 거절";
		String detail = "죄송합니다. 파티 참여가 거절되었습니다.";
		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_REJECTION.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}
		NotificationMulticastRequest request =
			partyMessageManager.createRejectionRequest(tokens, partyId, title, detail);
		notificationSender.send(request);
	}

	/**
	 * 아래 메서드들 파티장,파티멤버의 알림 내용 다른지에 따라 추후 수정 필요
	 */

	@Transactional
	public void sendAutoCloseNotification(List<Long> memberIds, Long partyId, Long managerId) {
		String title = "🎉 파티 자동 마감";
		String memberDetail = "참여 인원이 모두 차서 파티가 마감되었습니다.";
		String managerDetail = "축하합니다! 목표 인원 달성으로 파티가 자동 마감되었습니다.";

		memberIds.forEach(userId -> {
			String detail = userId.equals(managerId) ? managerDetail : memberDetail;
			notificationLogService.saveLog(userId, NotificationType.PARTY_AUTO_CLOSE.name(), title, detail);
		});

		List<String> tokens = fcmTokenService.getMulticastTokensAndLogIfEmpty(memberIds);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createAutoCloseRequest(tokens, partyId, title, memberDetail);
		notificationSender.send(request);
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
		notificationSender.send(request);
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
				notificationSender.send(memberRequest);
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
			notificationSender.send(managerRequest);
		}
	}

	@Transactional
	public void sendPartyCompleteNotification(List<Long> memberIds, Long partyId) {
		String title = "👋 파티 종료";
		String detail = "파티장이 수령 완료 처리했습니다. 파티가 종료되었습니다.";
		memberIds.forEach(userId ->
			notificationLogService.saveLog(userId, NotificationType.PARTY_COMPLETE.name(), title, detail)
		);

		List<String> tokens = fcmTokenService.getMulticastTokensAndLogIfEmpty(memberIds);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createPartyCompleteRequest(tokens, partyId, title, detail);
		notificationSender.send(request);
	}

	@Transactional
	public void sendNewPartyRequestNotification(Long managerId, Long partyId) {
		String title = "🔔 새 참여 요청";
		String detail = "새로운 참여 요청이 도착했습니다. 지금 승인해 주세요.";

		notificationLogService.saveLog(managerId, NotificationType.PARTY_NEW_REQUEST.name(), title, detail);

		List<String> tokens = fcmTokenService.getTokensAndLogIfEmpty(managerId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createNewPartyRequest(tokens, partyId, title, detail);
		notificationSender.send(request);
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
		notificationSender.send(request);
	}
}
