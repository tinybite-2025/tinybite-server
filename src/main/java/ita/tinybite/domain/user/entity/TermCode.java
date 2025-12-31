package ita.tinybite.domain.user.entity;

public enum TermCode {
    AGE_OVER_14,              // (필수) 만 14세 이상
    SERVICE_USE,              // (필수) 서비스 이용약관 동의
    ELECTRONIC_FINANCE,       // (필수) 전자금융거래 이용약관 동의
    PRIVACY_COLLECT,          // (필수) 개인정보 수집 이용 동의
    PRIVACY_PROVIDE,          // (필수) 개인정보 제공 동의
    MARKETING_RECEIVE         // (선택) 쇼핑정보 및 혜택 수신 동의
}
