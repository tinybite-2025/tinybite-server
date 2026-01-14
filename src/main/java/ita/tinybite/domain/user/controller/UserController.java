package ita.tinybite.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import ita.tinybite.domain.party.dto.response.PartyCardResponse;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.domain.user.dto.res.RejoinValidationResponse;
import ita.tinybite.domain.user.dto.res.UserResDto;
import ita.tinybite.domain.user.dto.res.WithDrawValidationResponse;
import ita.tinybite.domain.user.service.UserService;
import ita.tinybite.global.response.APIResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ita.tinybite.global.response.APIResponse.success;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/me")
    public APIResponse<UserResDto> getUser() {
        return success(userService.getUser());
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me")
    public APIResponse<?> updateUser(@Valid @RequestBody UpdateUserReqDto req) {
        userService.updateUser(req);
        return success();
    }

    @Operation(summary = "위치 정보 수정", description = "사용자의 현재 위치(위도, 경도)를 업데이트합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "위치 업데이트 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/location")
    public APIResponse<?> updateLocation(@RequestParam(defaultValue = "37.3623504988728") String latitude,
                                         @RequestParam(defaultValue = "127.117057453619") String longitude) {
        userService.updateLocation(latitude, longitude);
        return success();
    }

    @Operation(
            summary = "회원 탈퇴 가능 여부 확인",
            description = "진행 중인 파티가 있는지 확인하여 탈퇴 가능 여부를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/withdrawal/validate")
    public APIResponse<WithDrawValidationResponse> validateWithdrawal(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        WithDrawValidationResponse response = userService.validateWithdrawal(userId);
        return success(response);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/me")
    public APIResponse<?> deleteUser(@AuthenticationPrincipal Long userId) {
        userService.deleteUser(userId);
        return success();
    }

    @Operation(
            summary = "재가입 가능 여부 확인",
            description = "탈퇴 후 30일 이내인지 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공")
    })
    @GetMapping("/rejoin/validate")
    public APIResponse<RejoinValidationResponse> validateRejoin(
            @Parameter(description = "이메일", required = true)
            @RequestParam String email) {
        RejoinValidationResponse response = userService.validateRejoin(email);
        return success(response);
    }

    @Operation(
            summary = "호스팅 중인 파티 목록 조회",
            description = "현재 사용자가 호스트로 있는 활성 파티 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PartyCardResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/parties/hosting")
    public ResponseEntity<List<PartyCardResponse>> getHostingParties(
            @AuthenticationPrincipal Long userId) {
        List<PartyCardResponse> response = userService.getHostingParties(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "참가 중인 파티 목록 조회",
            description = "현재 사용자가 참가자로 있는 활성 파티 목록을 조회합니다. (호스트 제외)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PartyCardResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/parties/participating")
    public ResponseEntity<List<PartyCardResponse>> getParticipatingParties(
            @AuthenticationPrincipal Long userId) {
        List<PartyCardResponse> response = userService.getParticipatingParties(userId);
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

    @Operation(
            summary = "프로필 이미지 변경",
            description = "사용자의 프로필 이미지를 변경합니다. 이미지 URL을 전달받아 업데이트합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 이미지 변경 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 이미지 URL)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/me/profile-image")
    public ResponseEntity<Void> updateProfileImage(
            @Parameter(description = "변경할 프로필 이미지 URL", required = true, example = "https://example.com/image.jpg")
            @RequestBody String image,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId) {

        userService.updateProfileImage(userId, image);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "프로필 이미지 삭제",
            description = "사용자의 프로필 이미지를 삭제하고 기본 이미지로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 이미지 삭제 성공 (기본 이미지로 변경됨)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<Void> deleteProfileImage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId) {

        userService.deleteProfileImage(userId);
        return ResponseEntity.ok().build();
    }
}
