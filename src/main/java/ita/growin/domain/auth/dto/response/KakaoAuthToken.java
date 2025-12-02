package ita.growin.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class KakaoAuthToken {
    /** 토큰 요청에 필요한 인가 코드 */
    private String code;

    /** 인증 실패 시 반환되는 에러 코드 */
    private String error;

    /** 인증 실패 시 반환되는 에러 메시지 */
    private String error_description;

    /** CSRF 방지용 state 값 (선택적으로 사용) */
    private String state;
}
