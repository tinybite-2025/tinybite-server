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

    @Operation(summary = "활성 파티 목록 조회", description = "사용자가 참여 중인 활성 파티 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PartyResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/parties/active")
    public ResponseEntity<List<PartyResponse>> getActiveParties(
            @AuthenticationPrincipal Long userId) {
        List<PartyResponse> response = userService.getActiveParties(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
            @ApiResponse(responseCode = "400", description = "이미 사용 중인 닉네임",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/nickname/check")
    public APIResponse<?> validateNickname(@RequestParam String nickname) {
        userService.validateNickname(nickname);
        return success();
    }
}
