package ita.tinybite.domain.auth.dto.response;

import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class UserDto {
    private Long userId;
    private String email;
    private String nickname;
    private LoginType type;
    private UserStatus status;
    private String location;
    private String phone;
    private LocalDateTime createdAt;
    private Boolean isNewUser;

    public static UserDto from(User user) {
            return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .type(user.getType())
                .status(user.getStatus())
                .phone(user.getPhone())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .isNewUser(false)
                .build();
    }
}
