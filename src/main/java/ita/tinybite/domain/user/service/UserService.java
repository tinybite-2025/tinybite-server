package ita.tinybite.domain.user.service;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.domain.user.dto.res.UserResDto;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.AuthErrorCode;
import ita.tinybite.global.location.LocationService;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final SecurityProvider securityProvider;
    private final UserRepository userRepository;
    private final LocationService locationService;

    public UserService(SecurityProvider securityProvider,
                       UserRepository userRepository,
                       LocationService locationService) {
        this.securityProvider = securityProvider;
        this.userRepository = userRepository;
        this.locationService = locationService;
    }

    public UserResDto getUser() {
        User user = securityProvider.getCurrentUser();
        return UserResDto.of(user);
    }

    public void updateUser(UpdateUserReqDto req) {
        User user = securityProvider.getCurrentUser();
        user.update(req);
    }

    public void updateLocation(String latitude, String longitude) {
        User user = securityProvider.getCurrentUser();
        String location = locationService.getLocation(latitude, longitude);
        user.updateLocation(location);
    }

    public void deleteUser() {
        userRepository.delete(securityProvider.getCurrentUser());
    }

    public void validateNickname(String nickname) {
        if(userRepository.existsByNickname(nickname))
            throw BusinessException.of(AuthErrorCode.DUPLICATED_NICKNAME);
    }
}
