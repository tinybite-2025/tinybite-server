package ita.tinybite.domain.auth.dto.request;

import ita.tinybite.domain.user.constant.PlatformType;

public record GoogleAndAppleLoginReq(String idToken,
                                     PlatformType platformType) {
}
