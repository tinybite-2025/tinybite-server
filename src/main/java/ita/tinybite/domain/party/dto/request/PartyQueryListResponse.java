package ita.tinybite.domain.party.dto.request;

import ita.tinybite.domain.party.dto.response.PartyCardResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record PartyQueryListResponse(
        List<PartyCardResponse> parties,
        Boolean hasNext
) {
}
