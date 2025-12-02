package ita.growin.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;


@Getter
public class KakaoLoginRequest {

    @NotBlank(message = "Access Token은 필수입니다.")
    private String accessToken;

    private String deviceToken;
}