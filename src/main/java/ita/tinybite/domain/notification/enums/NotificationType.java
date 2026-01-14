package ita.tinybite.domain.notification.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum NotificationType {
	// 채팅
	CHAT_NEW_MESSAGE,
	CHAT_UNREAD_REMINDER,

	// 파티 참여
	PARTY_APPROVAL,
	PARTY_REJECTION,
	PARTY_AUTO_CLOSE,
	PARTY_ORDER_COMPLETE,
	PARTY_DELIVERY_REMINDER,
	PARTY_COMPLETE,

	// 파티 운영
	PARTY_NEW_REQUEST,
	PARTY_MEMBER_LEAVE,
	PARTY_MANAGER_DELIVERY_REMINDER,

	// 파티 참여 리마인드
	PENDING_APPROVAL_REMINDER,

	// 테스트 알림
	TEST_EVENT,

	// 마케팅 알림
	MARKETING_LOCAL_NEW_PARTY,
	MARKETING_WEEKLY_POPULAR,
	MARKETING_PROMOTION_EVENT;

}
