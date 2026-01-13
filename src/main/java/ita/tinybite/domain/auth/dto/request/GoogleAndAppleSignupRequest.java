package ita.tinybite.domain.auth.dto.request;

import ita.tinybite.domain.user.constant.PlatformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GoogleAndAppleSignupRequest(
        @NotBlank(message = "idToken은 필수입니다")
        String idToken,
        @NotBlank(message = "전화번호는 필수입니다")
        String phone,
        @NotBlank(message = "닉네임은 필수입니다")
        String nickname,
        // 위도
        String longitude,
        // 경도
        String latitude,
        @NotNull(message = "플랫폼정보는 필수입니다")
        PlatformType platform,
        @NotEmpty
        List<String> agreedTerms
) {
}
