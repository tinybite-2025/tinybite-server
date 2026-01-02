package ita.tinybite.domain.user.dto.res;

import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class PartyResponse {
    private Long id;
    private String title;
    private String description;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private PartyStatus status;
    private String hostUsername;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private boolean isHost;
    private ParticipantStatus participantStatus;

    public static PartyResponse from(Party party, int currentParticipants, boolean isHost, ParticipantStatus participantStatus) {
        return PartyResponse.builder()
                .id(party.getId())
                .title(party.getTitle())
                .description(party.getDescription())
                .maxParticipants(party.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .status(party.getStatus())
                .hostUsername(party.getHost().getNickname())
                .startDate(party.getCreatedAt())
                .endDate(party.getClosedAt())
                .createdAt(party.getCreatedAt())
                .isHost(isHost)
                .participantStatus(participantStatus)
                .build();
    }
}
