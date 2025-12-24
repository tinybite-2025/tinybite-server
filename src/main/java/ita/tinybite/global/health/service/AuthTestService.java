package ita.tinybite.global.health.service;

import ita.tinybite.domain.auth.entity.JwtTokenProvider;
import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthTestService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthTestService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String getUser(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() ->
        {
            User newUser = User.builder()
                    .email(email)
                    .type(LoginType.GOOGLE)
                    .status(UserStatus.ACTIVE)
                    .nickname(email)
                    .build();
            userRepository.save(newUser);
            return newUser;
        });

        return jwtTokenProvider.generateAccessToken(user);
    }
}
