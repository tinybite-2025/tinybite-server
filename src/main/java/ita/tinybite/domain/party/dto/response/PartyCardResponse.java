package ita.tinybite.domain.party.dto.response;
import ita.tinybite.domain.party.enums.PartyCategory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCardResponse {
    private Long partyId;
    private String thumbnailImage; // 첫 번째 이미지 또는 기본 이미지
    private String title;
    private Integer pricePerPerson; // 1/N 가격
    private String participantStatus; // "1/4명"
    private String distance; // "300m" or "1.2km" (화면 표시용)
    private Double distanceKm; // km 단위 거리 (정렬용)
    private String timeAgo; // "10분 전", "3시간 전"
    private Boolean isClosed; // 마감 여부
    private PartyCategory category;
    private LocalDateTime createdAt;
}