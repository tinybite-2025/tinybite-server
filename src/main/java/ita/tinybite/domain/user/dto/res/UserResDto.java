package ita.tinybite.domain.user.dto.res;

import ita.tinybite.domain.user.entity.User;

public record UserResDto(
        Long userId,
        String name,
        String location
) {

    public static UserResDto of(User user) {
        return new UserResDto(user.getUserId(), user.getNickname(), user.getLocation());
    }
}
