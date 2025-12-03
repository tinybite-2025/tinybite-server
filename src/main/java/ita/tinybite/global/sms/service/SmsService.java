package ita.tinybite.global.sms.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final String key;
    private final String secret;
    private static final String FROM = "01076029238";

    private DefaultMessageService messageService;

    public SmsService(@Value("${sms.api-key}") String key,
                      @Value("${sms.api-secret}") String secret) {
        this.key = key;
        this.secret = secret;
    }

    @PostConstruct
    public void init() {
        this.messageService = SolapiClient.INSTANCE.createInstance(key, secret);
    }

    public void send(String phone, String smsAuthCode) {
        Message message = createMessage(phone, smsAuthCode);

        try {
            messageService.send(message);
        } catch (SolapiMessageNotReceivedException | SolapiEmptyResponseException | SolapiUnknownException e) {
            throw new RuntimeException(e);
        }
    }

    private Message createMessage(String phone, String smsAuthCode) {
        Message message = new Message();
        message.setFrom(FROM);
        message.setTo(phone);
        message.setText(smsAuthCode);
        return message;
    }
}
