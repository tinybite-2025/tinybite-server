package ita.tinybite.domain.notification.service.facade;

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
	public void notifyNewChatMessage(
		Long targetUserId,
		Long chatRoomId,
		String senderName,
		String messageContent
	) {
		chatNotificationService.sendNewChatMessage(targetUserId, chatRoomId, senderName, messageContent);
	}

}
