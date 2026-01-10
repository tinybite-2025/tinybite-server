package ita.tinybite.domain.notification.service.facade;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ita.tinybite.domain.notification.service.ChatNotificationService;
import ita.tinybite.domain.notification.service.PartyNotificationService;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
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
	private final PartyParticipantRepository partyParticipantRepository;

	private final RedisTemplate<String, String> redisTemplate;

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

	public void notifyApproval(Long targetUserId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));
		partyNotificationService.sendApprovalNotification(targetUserId, party.getTitle(), partyId);
	}

	public void notifyRejection(Long targetUserId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));
		partyNotificationService.sendRejectionNotification(targetUserId, party.getTitle(), partyId);
	}

	// 인원 모집 완료
	public void notifyPartyAutoClose(List<Long> memberIds, Long partyId, Long managerId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));

		partyNotificationService.sendAutoCloseNotification(memberIds, party.getTitle(), partyId, managerId);
	}

	// 파티 종료
	public void notifyPartyComplete(List<Long> memberIds, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));

		partyNotificationService.sendPartyCompleteNotification(memberIds, party.getTitle(), partyId);
	}

	public void notifyOrderComplete(List<Long> memberIds, Long partyId) {
		partyNotificationService.sendOrderCompleteNotification(memberIds, partyId);
	}

	public void notifyDeliveryReminder(List<Long> memberIds, Long partyId, Long managerId) {
		partyNotificationService.sendDeliveryReminderNotification(memberIds, partyId, managerId);
	}

	public void notifyMemberLeave(Long managerId, Long partyId, String leaverName) {
		partyNotificationService.sendMemberLeaveNotification(managerId, partyId, leaverName);
	}

	// 1:1 채팅
	public void notifyOneToOneChat(Long targetUserId, Long chatRoomId, String senderName, String content) {
		chatNotificationService.sendOneToOneChatMessage(targetUserId, chatRoomId, senderName, content);
	}

	// 1:1 사진
	public void notifyOneToOneImage(Long targetUserId, Long chatRoomId, String senderName) {
		chatNotificationService.sendOneToOneChatImage(targetUserId, chatRoomId, senderName);
	}

	// 단체 채팅
	public void notifyGroupChat(Long targetUserId, Long chatRoomId, String partyTitle, String senderName, String content) {
		chatNotificationService.sendGroupChatMessage(targetUserId, chatRoomId, partyTitle, senderName, content);
	}

	// 단체 사진
	public void notifyGroupImage(Long targetUserId, Long chatRoomId, String partyTitle, String senderName) {
		chatNotificationService.sendGroupChatImage(targetUserId, chatRoomId, partyTitle, senderName);
	}

	// 승인 대기 리마인드 생성(레디스 등록)
	public void reservePartyRequestReminder(Long managerId, Long requesterId, Long partyId) {
		partyNotificationService.reservePendingApprovalReminder(
			managerId,
			requesterId,
			partyId
		);
	}

	// 스케줄러가 호출
	public void notifyPendingApprovalReminder(Long hostId, Long partyId, Long requesterId, String requesterNickname) {
		// PENDING 확인
		boolean isStillPending = partyParticipantRepository
			.existsByParty_IdAndUser_UserIdAndStatus(partyId, requesterId, ParticipantStatus.PENDING);

		if (isStillPending) {
			partyNotificationService.sendPendingApprovalReminder(hostId, requesterNickname, partyId);
		} else {
			// 이미 승인/거절되었다면 Redis에서 삭제
			String key = "pending_reminder:" + partyId + ":" + requesterId;
			redisTemplate.delete(key);
		}
	}

	// 스케줄러/채팅 서비스가 호출하며, 알림 도메인은 전송만 처리(보류)
	@Transactional
	public void notifyUnreadReminder(Long targetUserId, Long chatRoomId) {
		chatNotificationService.sendUnreadReminderNotification(targetUserId, chatRoomId);
	}

	@Transactional
	public void cancelPendingApprovalReminder(Long partyId, Long requesterId) {
		String key = "pending_reminder:" + partyId + ":" + requesterId;
		redisTemplate.delete(key);
	}
}
