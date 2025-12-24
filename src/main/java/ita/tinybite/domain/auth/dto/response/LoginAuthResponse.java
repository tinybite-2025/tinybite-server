package ita.tinybite.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginAuthResponse {
    private boolean signup;
    private AuthResponse authResponse;
}
