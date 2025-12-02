package ita.tinybite.global.sms.service;

import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.AuthErrorCode;
import ita.tinybite.global.sms.AuthCodeGenerator;
import ita.tinybite.global.sms.dto.req.CheckReqDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class SmsAuthService {

    private static final long EXPIRE_TIME = 60000L; // 1분
    private final SmsService smsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthCodeGenerator authCodeGenerator;

    public SmsAuthService(SmsService smsService, RedisTemplate<String, String> redisTemplate) {
        this.smsService = smsService;
        this.redisTemplate = redisTemplate;
        this.authCodeGenerator = AuthCodeGenerator.getInstance();
    }

    /**
     * 1. 인증코드 생성 <br>
     * 2. 주어진 폰번호로 인증코드 전송 <br>
     * 3. DB에 {번호, 인증코드}쌍으로 저장 or 메모리에 저장 (ttl 설정 고려하기) <br>
     */
    public void send(String phone) {
        validatePhoneNumber(phone);

        String smsAuthCode = authCodeGenerator.getAuthCode();
        smsService.send(phone, smsAuthCode);
        redisTemplate.opsForValue().set(phone, smsAuthCode, EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * req.phone으로 redis 조회 <br>
     * 조회한 authCode와 요청받은 authcode를 비교 <br>
     * 같으면 true, 다르면 false <br>
     */
    public boolean check(CheckReqDto req) {
        validatePhoneNumber(req.phone());

        String authCode = redisTemplate.opsForValue().get(req.phone());
        if(authCode == null) return false;
        return authCode.equals(req.authCode());
    }

    private void validatePhoneNumber(String phone) {
        if(!Pattern.matches("010-\\d{4}-\\d{4}", phone))
            throw new BusinessException(AuthErrorCode.INVALID_PHONE_NUMBER);
    }
}
