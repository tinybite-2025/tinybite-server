package ita.tinybite.domain.party.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostInfo {
    private Long userId;
    private String nickname;
    private String profileImage;
    private String neighborhood;
}
