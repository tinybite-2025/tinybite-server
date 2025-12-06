package ita.tinybite.global.sms.fake;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class FakeRedisTemplate extends RedisTemplate<String, String> {

    private final FakeValueOps ops = new FakeValueOps();

    @Override
    public ValueOperations<String, String> opsForValue() {
        return ops;
    }
}
