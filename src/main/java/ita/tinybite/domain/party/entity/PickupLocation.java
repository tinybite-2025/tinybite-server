package ita.tinybite.domain.party.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class PickupLocation {
    @NotBlank(message = "수령 장소는 필수입니다")
    @Size(max = 30)
    private String place;

    @NotNull(message = "위도는 필수입니다")
    private Double pickupLatitude;

    @NotNull(message = "경도는 필수입니다")
    private Double pickupLongitude;
}
