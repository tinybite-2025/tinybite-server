package ita.tinybite.domain.auth.dto.response;

import ita.tinybite.domain.user.dto.res.UserResDto;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResDto user;
}

