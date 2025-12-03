package ita.tinybite.domain.auth.service;

import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.UserErrorCode;
import ita.tinybite.global.exception.errorcode.CommonErrorCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityProvider {

    private final UserRepository userRepository;

    public SecurityProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return getUserFromContext();
    }

    private User getUserFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof UsernamePasswordAuthenticationToken auth)) {
            throw BusinessException.of(CommonErrorCode.UNAUTHORIZED);
        }

        Long userId = (Long) auth.getPrincipal();
        return userRepository.findById(userId).orElseThrow(() -> BusinessException.of(UserErrorCode.USER_NOT_EXISTS));
    }
}
