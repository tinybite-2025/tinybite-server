package ita.growin.global.util;

import ita.growin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class NicknameGenerator {

    private final UserRepository userRepository;
    private final Random random = new Random();

    public String generate() {
        String nickname;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            nickname = generateRandomNickname();
            attempts++;

            if (attempts >= maxAttempts) {
                // UUID 기반으로 전환
                nickname = "사용자" + System.currentTimeMillis();
            }
        } while (userRepository.existsByNickname(nickname));

        return nickname;
    }

    private String generateRandomNickname() {
        int number = random.nextInt(10000);
        return String.format("사용자%04d", number);
    }
}
