package ita.tinybite.domain.party.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "파티 대표 이미지 설정 요청")
public class PartyThumbnailUpdateRequest {

    @NotBlank(message = "대표 이미지 URL은 필수입니다")
    @Schema(description = "대표 이미지 URL", example = "https://example.com/image.jpg")
    private String thumbnailImage;
}
