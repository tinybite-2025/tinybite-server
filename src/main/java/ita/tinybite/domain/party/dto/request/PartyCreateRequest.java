package ita.tinybite.domain.party.dto.request;

import ita.tinybite.domain.party.enums.PartyCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCreateRequest {
    @NotBlank(message = "파티 제목은 필수입니다")
    @Size(max = 30, message = "파티 제목은 최대 30자까지 입력 가능합니다")
    private String title;

    @NotNull(message = "카테고리는 필수입니다")
    private PartyCategory category;

    @NotNull(message = "예상 총 주문 금액은 필수입니다")
    @Positive(message = "금액은 0보다 커야 합니다")
    private Integer totalPrice;

    @NotNull(message = "모집 인원은 필수입니다")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다")
    @Max(value = 10, message = "최대 10명까지 가능합니다")
    private Integer maxParticipants;

    @NotBlank(message = "수령 장소는 필수입니다")
    @Size(max = 30, message = "수령 장소는 최대 30자까지 입력 가능합니다")
    private String pickupLocation;

    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    private Double longitude;

    @Size(max = 5, message = "이미지는 최대 5장까지 업로드 가능합니다")
    private List<String> images;

    @URL(message = "올바른 URL 형식으로 입력해주세요")
    private String productLink;

    @Size(max = 60, message = "상세 설명은 최대 60자까지 입력 가능합니다")
    private String description;
}
