package ita.tinybite.global.sms.controller;

import ita.tinybite.global.response.APIResponse;
import ita.tinybite.global.sms.dto.req.CheckReqDto;
import ita.tinybite.global.sms.dto.req.SendReqDto;
import ita.tinybite.global.sms.service.SmsAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ita.tinybite.global.response.APIResponse.success;

@RestController
@RequestMapping("/api/v1/auth/sms")
public class SmsAuthController {

    private final SmsAuthService smsAuthService;

    public SmsAuthController(SmsAuthService smsAuthService) {
        this.smsAuthService = smsAuthService;
    }

    @PostMapping("/send")
    public APIResponse<?> send(@RequestBody SendReqDto req) {
        smsAuthService.send(req.phone());
        return success();
    }

    @PostMapping("/check")
    public APIResponse<?> check(@RequestBody CheckReqDto req) {
        smsAuthService.check(req);
        return success();
    }
}
