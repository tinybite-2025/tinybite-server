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

	// 멀티캐스트-대상 유저의 모든 토큰에 전송(파티 참여 승인)
	public NotificationMulticastRequest createApprovalRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put("partyId", String.valueOf(partyId));
		data.put("eventType", NotificationType.PARTY_APPROVAL.name());

		String title = "🎉 파티 참여 승인";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

	// 멀티 캐스트(파티 자동 마감 알림)
	// 참여 인원이 모두 차서 파티가 마감되었습니다. -> detail로 주입
	public NotificationMulticastRequest createAutoCloseRequest(List<String> tokens, Long partyId, String detail) {

		Map<String, String> data = new HashMap<>();
		data.put("partyId", String.valueOf(partyId));
		data.put("eventType", NotificationType.PARTY_AUTO_CLOSE.name());

		String title = "🚨 파티 자동 마감";
		return requestConverter.toMulticastRequest(tokens, title, detail, data);
	}

}
