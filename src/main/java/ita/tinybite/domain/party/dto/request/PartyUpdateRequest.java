package ita.tinybite.domain.party.dto.request;

import ita.tinybite.domain.party.entity.PickupLocation;
import ita.tinybite.domain.party.enums.PartyCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyUpdateRequest {

    @Size(max = 30, message = "파티 제목은 최대 30자까지 입력 가능합니다")
    private String title;

    @Positive(message = "금액은 0보다 커야 합니다")
    private Integer totalPrice;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다")
    @Max(value = 10, message = "최대 10명까지 가능합니다")
    private Integer maxParticipants;

    @Size(max = 30, message = "수령 장소는 최대 30자까지 입력 가능합니다")
    private PickupLocation pickupLocation;

    private PartyCategory category;

//    @Pattern(regexp = "^(https?://)?.*", message = "올바른 URL 형식으로 입력해주세요")
    private String productLink;

    // 항상 수정 가능한 필드
    @Size(max = 60, message = "상세 설명은 최대 60자까지 입력 가능합니다")
    private String description;

    @Size(max = 5, message = "이미지는 최대 5장까지 업로드 가능합니다")
    private List<String> images;
}