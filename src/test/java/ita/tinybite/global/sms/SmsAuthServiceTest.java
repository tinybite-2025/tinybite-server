package ita.tinybite.global.sms;

import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.sms.fake.FakeRedisTemplate;
import ita.tinybite.global.sms.fake.FakeSmsService;
import ita.tinybite.global.sms.service.SmsAuthService;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.*;


class SmsAuthServiceTest {

    // Fake 객체
    private final FakeSmsService fakeSmsService = new FakeSmsService();
    private final FakeRedisTemplate fakeRedisTemplate = new FakeRedisTemplate();

    // 테스트 객체
    private final SmsAuthService smsAuthService = new SmsAuthService(fakeSmsService, fakeRedisTemplate);

    private static final String SUCCESS_PHONE_NUMBER = "010-1234-4321";
    private static final String[] FAIL_PHONE_NUMBER = {"010-1234-43211", "asdf", "010-12344-123", "123-1234-1234"};

    @Test
    void should_success_when_smsAuth_send() {
        smsAuthService.send(SUCCESS_PHONE_NUMBER);
        assertThat(fakeRedisTemplate.opsForValue().get(SUCCESS_PHONE_NUMBER)).isNotNull();
    }

    @Test
    void should_fail_when_smsAuth_send_with_invalid_phone() {
        for (String phone : FAIL_PHONE_NUMBER) {
            assertThatThrownBy(() -> smsAuthService.send(phone))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
