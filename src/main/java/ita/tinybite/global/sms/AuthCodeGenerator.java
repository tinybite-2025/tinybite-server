package ita.tinybite.global.sms;

import java.util.concurrent.ThreadLocalRandom;

public class AuthCodeGenerator {

    private static final AuthCodeGenerator INSTANCE = new AuthCodeGenerator();

    private AuthCodeGenerator() {}

    public static AuthCodeGenerator getInstance() {
        return INSTANCE;
    }


    public String getAuthCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
