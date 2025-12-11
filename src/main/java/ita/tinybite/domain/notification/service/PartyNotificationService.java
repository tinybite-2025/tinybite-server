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

	// 파티 참여 승인
	@Transactional
	public void sendApprovalNotification(Long targetUserId, Long partyId) {

		String detail = "파티 참여가 승인되었습니다! 지금 확인하세요.";
		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_APPROVAL.name(), detail);

		List<String> tokens = getTokens(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}
		NotificationMulticastRequest request =
			partyMessageManager.createApprovalRequest(tokens, partyId, detail);
		notificationSender.send(request);
	}

	// 파티 참여 거절
	@Transactional
	public void sendRejectionNotification(Long targetUserId, Long partyId) {

		String detail = "죄송합니다. 파티 참여가 거절되었습니다.";
		notificationLogService.saveLog(targetUserId, NotificationType.PARTY_REJECTION.name(), detail);

		List<String> tokens = getTokens(targetUserId);
		if (tokens.isEmpty()) {
			return;
		}
		NotificationMulticastRequest request =
			partyMessageManager.createRejectionRequest(tokens, partyId, detail);
		notificationSender.send(request);
	}

	/**
	 * 아래 메서드들 파티장,파티멤버의 알림 내용 다른지에 따라 추후 수정 필요
	 */

	// 파티 자동 마감
	@Transactional
	public void sendAutoCloseNotification(List<Long> memberIds, Long partyId, Long managerId) {

		String memberDetail = "참여 인원이 모두 차서 파티가 마감되었습니다.";
		String managerDetail = "축하합니다! 목표 인원 달성으로 파티가 자동 마감되었습니다.";

		memberIds.forEach(userId -> {
			String detail = userId.equals(managerId) ? managerDetail : memberDetail;
			notificationLogService.saveLog(userId, NotificationType.PARTY_AUTO_CLOSE.name(), detail);
		});

		List<String> tokens = getMulticastTokens(memberIds);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createAutoCloseRequest(tokens, partyId, memberDetail);
		notificationSender.send(request);
	}

	// 주문 완료
	@Transactional
	public void sendOrderCompleteNotification(List<Long> memberIds, Long partyId) {

		String detail = "파티장이 상품 주문을 완료했습니다!";
		memberIds.forEach(userId ->
			notificationLogService.saveLog(userId, NotificationType.PARTY_ORDER_COMPLETE.name(), detail)
		);

		List<String> tokens = getMulticastTokens(memberIds);
		if (tokens.isEmpty()) {
			return;
		}
		NotificationMulticastRequest request =
			partyMessageManager.createOrderCompleteRequest(tokens, partyId, detail);
		notificationSender.send(request);
	}

	// 수령 준비
	@Transactional
	public void sendDeliveryReminderNotification(List<Long> memberIds, Long partyId, Long managerId) {

		// 파티 멤버
		String memberDetail = "수령 시간 30분 전입니다! 늦지 않게 준비해주세요.";
		List<Long> commonMembers = memberIds.stream()
			.filter(id -> !id.equals(managerId))
			.toList();

		if (!commonMembers.isEmpty()) {
			commonMembers.forEach(userId ->
				notificationLogService.saveLog(userId, NotificationType.PARTY_DELIVERY_REMINDER.name(), memberDetail)
			);

			List<String> memberTokens = getMulticastTokens(commonMembers);
			if (!memberTokens.isEmpty()) {
				NotificationMulticastRequest memberRequest =
					partyMessageManager.createDeliveryReminderRequest(memberTokens, partyId, memberDetail);
				notificationSender.send(memberRequest);
			}
		}

		// 파티장
		String managerType = NotificationType.PARTY_MANAGER_DELIVERY_REMINDER.name();
		String managerDetail = "수령 시간이 30분 남았습니다. 수령 장소로 이동해주세요!";

		notificationLogService.saveLog(managerId, managerType, managerDetail);

		List<String> managerTokens = getTokens(managerId);
		if (!managerTokens.isEmpty()) {
			NotificationMulticastRequest managerRequest =
				partyMessageManager.createManagerDeliveryReminderRequest(managerTokens, partyId, managerDetail);
			notificationSender.send(managerRequest);
		}
	}

	// 파티 종료
	@Transactional
	public void sendPartyCompleteNotification(List<Long> memberIds, Long partyId) {

		String detail = "파티장이 수령 완료 처리했습니다. 파티가 종료되었습니다.";
		memberIds.forEach(userId ->
			notificationLogService.saveLog(userId, NotificationType.PARTY_COMPLETE.name(), detail)
		);

		List<String> tokens = getMulticastTokens(memberIds);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createPartyCompleteRequest(tokens, partyId, detail);
		notificationSender.send(request);
	}

	// 새 참여 요청
	@Transactional
	public void sendNewPartyRequestNotification(Long managerId, Long partyId) {
		String detail = "새로운 참여 요청이 도착했습니다. 지금 승인해 주세요.";

		notificationLogService.saveLog(managerId, NotificationType.PARTY_NEW_REQUEST.name(), detail);

		List<String> tokens = getTokens(managerId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createNewPartyRequest(tokens, partyId, detail);
		notificationSender.send(request);
	}


	// 파티원 나가기
	@Transactional
	public void sendMemberLeaveNotification(Long managerId, Long partyId, String leaverName) {
		String detail = leaverName + "님이 파티에서 나갔습니다.";

		notificationLogService.saveLog(managerId, NotificationType.PARTY_MEMBER_LEAVE.name(), detail);

		List<String> tokens = getTokens(managerId);
		if (tokens.isEmpty()) {
			return;
		}

		NotificationMulticastRequest request =
			partyMessageManager.createMemberLeaveRequest(tokens, partyId, detail);
		notificationSender.send(request);
	}


	// 단일 사용자 토큰 조회
	private List<String> getTokens(Long targetUserId) {
		List<String> tokens = fcmTokenService.getTokensByUserId(targetUserId);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 ID: {}에 유효한 FCM 토큰이 없습니다. (푸시 전송 Skip)", targetUserId);
		}
		return tokens;
	}

	// 다중 사용자 토큰 조회
	private List<String> getMulticastTokens(List<Long> userIds) {
		List<String> tokens = fcmTokenService.getTokensByUserIds(userIds);
		if (tokens.isEmpty()) {
			log.warn("알림 대상 사용자 목록(IDs: {})에 유효한 FCM 토큰이 없습니다. (푸시 전송 Skip)", userIds);
		}
		return tokens;
	}
}
