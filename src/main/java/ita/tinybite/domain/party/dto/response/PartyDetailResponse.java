package ita.tinybite.domain.party.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ita.tinybite.domain.party.entity.PickupLocation;
import ita.tinybite.domain.party.enums.PartyCategory;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetailResponse {

    private Long partyId;
    private String title;
    private PartyCategory category;
    private String timeAgo;
    private String town;

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

    private String thumbnailImageDetail;

    // 이미지 (최대 5장)
    private List<String> images;

    // 상태
    private Boolean isClosed;
    private Boolean isParticipating; // 현재 사용자가 참여 중인지

    // 현재 유저가 해당 파티에 참여중이라면, 그룹 채팅방 아이디 반환
    private Long groupChatRoomId;
}
