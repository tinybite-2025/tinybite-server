package ita.tinybite.domain.user.service;

import ita.tinybite.domain.auth.service.AuthService;
import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.domain.user.dto.res.UserResDto;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.domain.user.service.fake.FakeLocationService;
import ita.tinybite.domain.user.service.fake.FakeSecurityProvider;
import ita.tinybite.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    // Fake 객체
    private FakeSecurityProvider securityProvider;
    private FakeLocationService locationService;

    // 테스트 객체
    private UserService userService;

    @BeforeEach
    void setUp() {
        securityProvider = new FakeSecurityProvider(userRepository);
        locationService = new FakeLocationService();
        userService = new UserService(securityProvider, userRepository, locationService);

        User user = User.builder()
                .email("yyytir777@gmail.com")
                .nickname("임원재")
                .location("분당구 정자동")
                .status(UserStatus.ACTIVE)
                .type(LoginType.KAKAO)
                .build();

        userRepository.save(user);
        securityProvider.setCurrentUser(user);
    }

    @Test
    void getUser() {
        UserResDto user = userService.getUser();
        assertThat(user).isNotNull();
    }

    @Test
    void updateUser() {
        // given
        UpdateUserReqDto req = new UpdateUserReqDto("updated_nickname");

        // when
        userService.updateUser(req);

        // then
        assertThat(securityProvider.getCurrentUser().getNickname()).isEqualTo("updated_nickname");
    }

    @Test
    void updateLocation() {
        // given
        String latitude = "12.123145"; String longitude = "123.123123";

        // when
        userService.updateLocation(latitude, longitude);

        // then
        assertThat(securityProvider.getCurrentUser().getLocation()).isEqualTo(locationService.getLocation(latitude, longitude));
    }

    @Test
    void deleteUser() {
        // when
        User currentUser = securityProvider.getCurrentUser();
        userService.deleteUser();

        // then
        assertThat(userRepository.findById(currentUser.getUserId())).isEmpty();
    }

    @Test
    void validateNickname() {
        assertThatThrownBy(() -> authService.validateNickname("임원재"))
                .isInstanceOf(BusinessException.class);

        assertThatNoException().isThrownBy(() -> authService.validateNickname("임원재1"));
    }
}