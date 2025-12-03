package ita.tinybite.global.sms.fake;

import ita.tinybite.global.sms.service.SmsService;

public class FakeSmsService extends SmsService {

    public FakeSmsService() {
        super("key", "secret");
    }

    @Override
    public void send(String phone, String smsAuthCode) {

    }
}
