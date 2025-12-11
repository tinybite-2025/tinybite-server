package ita.tinybite.domain.notification.service.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ita.tinybite.domain.notification.converter.NotificationRequestConverter;
import ita.tinybite.domain.notification.dto.request.NotificationMulticastRequest;
import ita.tinybite.domain.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PartyMessageManager {

	private final NotificationRequestConverter requestConverter;

	private static final String KEY_PARTY_ID = "partyId";
	private static final String KEY_EVENT_TYPE = "eventType";

	public NotificationMulticastRequest createApprovalRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_APPROVAL.name());

		String title = "🎉 파티 참여 승인";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createRejectionRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_REJECTION.name());

		String title = "파티 참여 거절";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createAutoCloseRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_AUTO_CLOSE.name());

		String title = "🚨 파티 자동 마감";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createOrderCompleteRequest(List<String> tokens, Long partyId, String detail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_ORDER_COMPLETE.name());

		String title = "✅ 상품 주문 완료";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createDeliveryReminderRequest(List<String> memberTokens, Long partyId, String memberDetail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_DELIVERY_REMINDER.name());

		String title = "⏰ 수령 준비 알림";
		return requestConverter.toMulticastRequest(memberTokens, title, memberDetail, data);
	}

	public NotificationMulticastRequest createManagerDeliveryReminderRequest(List<String> managerTokens, Long partyId, String managerDetail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_MANAGER_DELIVERY_REMINDER.name());

		String title = "📍 수령 장소 이동 알림";
		return requestConverter.toMulticastRequest(managerTokens, title, managerDetail, data);
	}

	public NotificationMulticastRequest createPartyCompleteRequest(List<String> tokens, Long partyId, String detail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_COMPLETE.name());

		String title = "👋 파티 종료";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createNewPartyRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_NEW_REQUEST.name());

		String title = "🔔 새 참여 요청";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createMemberLeaveRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_MEMBER_LEAVE.name());

		String title = "⚠️ 파티원 이탈";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}
}
