package ita.growin.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDto user;
}

