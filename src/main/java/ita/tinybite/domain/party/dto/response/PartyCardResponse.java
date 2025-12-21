package ita.tinybite.domain.party.dto.response;
import ita.tinybite.domain.party.enums.PartyCategory;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCardResponse {
    private Long partyId;
    private String thumbnailImage;
    private String title;
    private Integer pricePerPerson;
    private String participantStatus;
    private String distance;
    private String timeAgo;
    private Boolean isClosed;
    private PartyCategory category;
}