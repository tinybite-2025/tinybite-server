package ita.growin.domain.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
public class KakaoUserInfo {
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    public static class KakaoAccount {
        private String email;

        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;

        private Profile profile;

        @Getter
        @Setter
        public static class Profile {
            private String nickname;
        }
    }
}
