package ita.tinybite.domain.auth.dto.request;

import ita.tinybite.domain.user.constant.PlatformType;

public record GoogleAndAppleSignupRequest(
        String idToken,
        String phone,
        String nickname,
        String location,
        PlatformType platform
) {
}
