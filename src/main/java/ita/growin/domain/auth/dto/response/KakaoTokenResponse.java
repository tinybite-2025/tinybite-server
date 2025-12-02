package ita.growin.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class KakaoTokenResponse {
    /** 토큰 타입 (보통 "bearer") */
    private String token_type;

    /** 사용자 액세스 토큰 */
    private String access_token;

    /** 액세스 토큰 만료 시간(초 단위) */
    private Integer expires_in;

    /** 리프레시 토큰 */
    private String refresh_token;

    /** 리프레시 토큰 만료 시간(초 단위) */
    private Integer refresh_token_expires_in;

    /** 인증된 정보 범위(scope). 공백으로 구분됨 */
    private String scope;
}
