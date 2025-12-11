package ita.tinybite.domain.notification.service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.service.ChatNotificationService;
import ita.tinybite.domain.notification.service.PartyNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비즈니스 서비스(Party, Chat)로부터 요청을 받아
 * 하위 NotificationService로 위임
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationFacade {

	private final PartyNotificationService partyNotificationService;
	private final ChatNotificationService chatNotificationService;

	@Transactional
	public void notifyApproval(Long targetUserId, Long partyId) {
		partyNotificationService.sendApprovalNotification(targetUserId, partyId);
	}

	@Transactional
	public void notifyRejection(Long targetUserId, Long partyId) {
		partyNotificationService.sendRejectionNotification(targetUserId, partyId);
	}

	@Transactional
	public void notifyPartyAutoClose(List<Long> memberIds, Long partyId, Long managerId) {
		partyNotificationService.sendAutoCloseNotification(memberIds, partyId, managerId);
	}

	@Transactional
	public void notifyOrderComplete(List<Long> memberIds, Long partyId) {
		partyNotificationService.sendOrderCompleteNotification(memberIds, partyId);
	}

	@Transactional
	public void notifyDeliveryReminder(List<Long> memberIds, Long partyId, Long managerId) {
		partyNotificationService.sendDeliveryReminderNotification(memberIds, partyId, managerId);
	}

	@Transactional
	public void notifyPartyComplete(List<Long> memberIds, Long partyId) {
		partyNotificationService.sendPartyCompleteNotification(memberIds, partyId);
	}

	@Transactional
	public void notifyNewPartyRequest(Long managerId, Long partyId) {
		partyNotificationService.sendNewPartyRequestNotification(managerId, partyId);
	}

	@Transactional
	public void notifyMemberLeave(Long managerId, Long partyId, String leaverName) {
		partyNotificationService.sendMemberLeaveNotification(managerId, partyId, leaverName);
	}

	@Transactional
	public void notifyNewChatMessage(
		Long targetUserId,
		Long chatRoomId,
		String senderName,
		String messageContent
	) {
		chatNotificationService.sendNewChatMessage(targetUserId, chatRoomId, senderName, messageContent);
	}

	// 스케줄러/채팅 서비스가 호출하며, 알림 도메인은 전송만 처리
	@Transactional
	public void notifyUnreadReminder(Long targetUserId, Long chatRoomId) {
		chatNotificationService.sendUnreadReminderNotification(targetUserId, chatRoomId);
	}

}
