package ita.tinybite.domain.party.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class PartyInfo {
    private Long id;
    private String title;
    private HostInfo host;
}
