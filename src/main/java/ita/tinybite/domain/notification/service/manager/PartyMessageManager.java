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

	public NotificationMulticastRequest createApprovalRequest(List<String> tokens, Long partyId, String title, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_APPROVAL.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createRejectionRequest(List<String> tokens, Long partyId, String title, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_REJECTION.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createAutoCloseRequest(List<String> tokens, Long partyId, String title, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_AUTO_CLOSE.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createOrderCompleteRequest(List<String> tokens, Long partyId, String title, String detail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_ORDER_COMPLETE.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createDeliveryReminderRequest(List<String> memberTokens, Long partyId, String title, String memberDetail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_DELIVERY_REMINDER.name());

		return requestConverter.toMulticastRequest(memberTokens, title, memberDetail, data);
	}

	public NotificationMulticastRequest createManagerDeliveryReminderRequest(List<String> managerTokens, Long partyId, String title, String managerDetail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_MANAGER_DELIVERY_REMINDER.name());

		return requestConverter.toMulticastRequest(managerTokens, title, managerDetail, data);
	}

	public NotificationMulticastRequest createPartyCompleteRequest(List<String> tokens, Long partyId, String title, String detail) {
		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_COMPLETE.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createNewPartyRequest(List<String> tokens, Long partyId, String title, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_NEW_REQUEST.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	public NotificationMulticastRequest createMemberLeaveRequest(List<String> tokens, Long partyId, String title, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put(KEY_PARTY_ID, String.valueOf(partyId));
		data.put(KEY_EVENT_TYPE, NotificationType.PARTY_MEMBER_LEAVE.name());

		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}
}
