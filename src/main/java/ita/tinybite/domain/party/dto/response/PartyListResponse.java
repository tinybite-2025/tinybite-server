package ita.tinybite.domain.party.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyListResponse {

    private List<PartyCardResponse> activeParties; // 진행 중인 파티
    private List<PartyCardResponse> closedParties; // 마감된 파티
    private Boolean hasNext;
    private Integer totalCount;
}