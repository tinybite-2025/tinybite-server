package ita.tinybite.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class KakaoSignupRequest {

    @NotBlank(message = "Code는 필수입니다.")
    private String code;

    @NotNull(message = "동네 정보는 필수입니다.")
    private String location;

    @NotNull(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotNull(message = "전화번호는 필수입니다.")
    private String phone;

    private String deviceToken;
}