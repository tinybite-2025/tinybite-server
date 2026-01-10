package ita.tinybite.domain.notification.service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.service.ChatNotificationService;
import ita.tinybite.domain.notification.service.PartyNotificationService;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.repository.PartyRepository;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.PartyErrorCode;
import ita.tinybite.global.exception.errorcode.UserErrorCode;
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

	private final PartyRepository partyRepository;
	private final UserRepository userRepository;

	@Transactional
	public void notifyNewPartyRequest(Long managerId, Long requesterId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));

		User requester = userRepository.findById(requesterId)
			.orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_EXISTS));

		partyNotificationService.sendNewPartyRequestNotification(
			managerId,
			requester.getNickname(),
			party.getTitle(),
			partyId
		);
	}

	@Transactional
	public void notifyApproval(Long targetUserId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));
		partyNotificationService.sendApprovalNotification(targetUserId, party.getTitle(), partyId);
	}

	@Transactional
	public void notifyRejection(Long targetUserId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));
		partyNotificationService.sendRejectionNotification(targetUserId, party.getTitle(), partyId);
	}

	// 인원 모집 완료
	@Transactional
	public void notifyPartyAutoClose(List<Long> memberIds, Long partyId, Long managerId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));

		partyNotificationService.sendAutoCloseNotification(memberIds, party.getTitle(), partyId, managerId);
	}

	// 파티 종료
	@Transactional
	public void notifyPartyComplete(List<Long> memberIds, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));

		partyNotificationService.sendPartyCompleteNotification(memberIds, party.getTitle(), partyId);
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
