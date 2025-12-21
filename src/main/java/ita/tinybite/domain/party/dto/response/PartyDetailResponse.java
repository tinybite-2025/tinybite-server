package ita.tinybite.domain.party.dto.response;

import ita.tinybite.domain.party.entity.PickupLocation;
import ita.tinybite.domain.party.enums.PartyCategory;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyDetailResponse {

    private Long partyId;
    private String title;
    private PartyCategory category;
    private String timeAgo;

    // 파티장 정보
    private HostInfo host;

    // 수령 정보
    private PickupLocation pickupLocation;
    private String distance; // "300m"

    // 인원 정보
    private Integer currentParticipants; // 승인된 인원
    private Integer maxParticipants;
    private Integer remainingSlots; // 남은 자리

    // 가격 정보
    private Integer pricePerPerson;
    private Integer totalPrice;

    // 상품 링크 (장보기/생활용품만)
    private ProductLink productLink;

    // 설명
    private String description;

    // 이미지 (최대 5장)
    private List<String> images;

    // 상태
    private Boolean isClosed;
    private Boolean isParticipating; // 현재 사용자가 참여 중인지
}
