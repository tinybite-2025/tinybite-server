package ita.tinybite.domain.user.controller;

import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.domain.user.service.UserService;
import ita.tinybite.global.response.APIResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static ita.tinybite.global.response.APIResponse.success;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public APIResponse<?> getUser() {
        return success(userService.getUser());
    }

    @PatchMapping("/me")
    public APIResponse<?> updateUser(@Valid @RequestBody UpdateUserReqDto req) {
        userService.updateUser(req);
        return success();
    }

    @PatchMapping("/me/location")
    public APIResponse<?> updateLocation(@RequestParam(defaultValue = "37.3623504988728") String latitude,
                                         @RequestParam(defaultValue = "127.117057453619") String longitude) {
        userService.updateLocation(latitude, longitude);
        return success();
    }

    @DeleteMapping("/me")
    public APIResponse<?> deleteUser() {
        userService.deleteUser();
        return success();
    }

    @GetMapping("/nickname/check")
    public APIResponse<?> validateNickname(@RequestParam String nickname) {
        userService.validateNickname(nickname);
        return success();
    }
}
