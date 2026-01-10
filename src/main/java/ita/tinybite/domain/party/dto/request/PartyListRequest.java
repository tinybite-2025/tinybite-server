package ita.tinybite.domain.party.dto.request;

import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.enums.PartySortType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartyListRequest {
    private PartyCategory category;  // 필터: 카테고리
    private PartySortType sortType;  // 정렬: 최신순/거리순

    private Integer page;
    private Integer size;

    // 거리순 정렬을 위한 현재 위치 (선택)
    private Double userLat;
    private Double userLon;
}
