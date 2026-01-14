package ita.tinybite.domain.party.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ita.tinybite.domain.party.dto.request.PartyCreateRequest;
import ita.tinybite.domain.party.dto.request.PartyListRequest;
import ita.tinybite.domain.party.dto.request.PartyQueryListResponse;
import ita.tinybite.domain.party.dto.request.PartyUpdateRequest;
import ita.tinybite.domain.party.dto.response.ChatRoomResponse;
import ita.tinybite.domain.party.dto.response.PartyDetailResponse;
import ita.tinybite.domain.party.dto.response.PartyListResponse;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.enums.PartySortType;
import ita.tinybite.domain.party.service.PartySearchService;
import ita.tinybite.domain.party.service.PartyService;
import ita.tinybite.global.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ita.tinybite.global.response.APIResponse.*;

@Tag(name = "파티 API", description = "파티 생성, 조회, 참여 관련 API")
@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;
    private final PartySearchService partySearchService;


    @Operation(
            summary = "파티 참여",
            description = "파티에 참여 신청합니다. 파티장의 승인이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "참여 신청 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (마감된 파티, 인원 초과 등)",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파티를 찾을 수 없음",
                    content = @Content
            )
    })
    @PostMapping("/{partyId}/join")
    public APIResponse<Long> joinParty(
            @PathVariable Long partyId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return success(partyService.joinParty(partyId, userId));
    }


    /**
     * 파티 탈퇴
     */
    @Operation(
            summary = "파티 탈퇴",
            description = "현재 참가 중인 파티에서 탈퇴합니다. 탈퇴 시 인원이 줄어들면 모집 완료 상태가 다시 모집 중으로 변경될 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "파티 탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"파티에서 탈퇴했습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (파티에 참가하지 않은 사용자)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"파티에 참가하지 않은 사용자입니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파티를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"파티를 찾을 수 없습니다.\"}")
                    )
            )
    })
    @DeleteMapping("/{partyId}/leave")
    public ResponseEntity<?> leaveParty(
            @PathVariable Long partyId,
            @AuthenticationPrincipal Long userId) {

        partyService.leaveParty(partyId, userId);
        return ResponseEntity.ok("파티에서 탈퇴했습니다.");
    }


    /**
     * 파티 모집 완료
     */

    @Operation(
            summary = "파티 모집 완료 처리",
            description = "파티 관리자가 정원 미달이어도 수동으로 모집을 완료 처리합니다. 모집 중 상태의 파티만 완료 처리할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "모집 완료 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"모집이 완료되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (모집 중 상태가 아님)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"모집 중인 파티만 완료 처리할 수 있습니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (관리자가 아님)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"파티 관리자만 모집을 완료할 수 있습니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파티를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"파티를 찾을 수 없습니다.\"}")
                    )
            )
    })
    @PatchMapping("/{partyId}/complete")
    public ResponseEntity<?> completeRecruitment(
            @PathVariable Long partyId,
            @AuthenticationPrincipal Long userId) {

        partyService.completeRecruitment(partyId, userId);
        return ResponseEntity.ok("모집이 완료되었습니다.");
    }

    @Operation(summary = "참여 승인", description = "파티장이 참여를 승인하면 단체 채팅방에 자동 입장됩니다")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "승인 성공",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "403",
                description = "파티장 권한 없음",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "404",
                description = "파티 또는 참여자를 찾을 수 없음",
                content = @Content
        )
    })
    @PostMapping("/{partyId}/participants/{participantId}/approve")
    public ResponseEntity<Void> approveParticipant(
            @PathVariable Long partyId,
            @PathVariable Long participantId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        partyService.approveParticipant(partyId, participantId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "참여 거절", description = "파티장이 참여를 거절합니다")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "거절 성공",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "403",
                description = "파티장 권한 없음",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "404",
                description = "파티 또는 참여자를 찾을 수 없음",
                content = @Content
        )
    })
    @PostMapping("/{partyId}/participants/{participantId}/reject")
    public ResponseEntity<Void> rejectParticipant(
            @PathVariable Long partyId,
            @PathVariable Long participantId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        partyService.rejectParticipant(partyId, participantId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "단체 채팅방 조회", description = "승인된 참여자가 단체 채팅방을 조회합니다")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ChatRoomResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "403",
                description = "승인된 참여자가 아님",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "404",
                description = "파티 또는 채팅방을 찾을 수 없음",
                content = @Content
        )
    })
    @GetMapping("/{partyId}/chat/group")
    public ResponseEntity<ChatRoomResponse> getGroupChatRoom(
            @PathVariable Long partyId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        ChatRoomResponse chatRoom = partyService.getGroupChatRoom(partyId, userId);

        return ResponseEntity.ok(chatRoom);
    }

    @Operation(summary = "결산 가능 여부", description = "목표 인원 달성 시 true 반환")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = Boolean.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "파티를 찾을 수 없음",
                content = @Content
        )
    })
    @GetMapping("/{partyId}/can-settle")
    public ResponseEntity<Boolean> canSettle(@PathVariable Long partyId) {
        boolean canSettle = partyService.canSettle(partyId);
        return ResponseEntity.ok(canSettle);
    }

    @Operation(summary = "파티 결산", description = "목표 인원 달성 후 파티를 마감합니다")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "결산 성공",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "400",
                description = "결산 조건 미충족",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "403",
                description = "파티장 권한 없음",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "404",
                description = "파티를 찾을 수 없음",
                content = @Content
        )
   })
    @PostMapping("/{partyId}/settle")
    public ResponseEntity<Void> settleParty(
            @PathVariable Long partyId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        partyService.settleParty(partyId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "승인 대기 목록", description = "파티장이 승인 대기 중인 참여자 목록을 조회합니다")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PartyParticipant.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "403",
                description = "파티장 권한 없음",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "404",
                description = "파티를 찾을 수 없음",
                content = @Content
        )
    })
    @GetMapping("/{partyId}/participants/pending")
    public ResponseEntity<List<PartyParticipant>> getPendingParticipants(
            @PathVariable Long partyId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        List<PartyParticipant> participants = partyService.getPendingParticipants(partyId, userId);

        return ResponseEntity.ok(participants);
    }

    /**
     * 파티 목록 조회 (홈 화면)
     */
    @GetMapping
    public ResponseEntity<PartyListResponse> getPartyList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "파티 카테고리",
                    example = "ALL",
                    schema = @Schema(allowableValues = {"ALL", "DELIVERY", "GROCERY", "HOUSEHOLD"})
            )
            @RequestParam(defaultValue = "ALL") PartyCategory category,
            @RequestParam(required = false, defaultValue = "LATEST") PartySortType sortType,
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude
            ) {
        Double lat = null;
        Double lon = null;

        if (latitude != null && longitude != null) {
            try {
                lat = Double.parseDouble(latitude);
                lon = Double.parseDouble(longitude);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("위치 정보 형식이 올바르지 않습니다. userLat: %s, userLon: %s", latitude, longitude)
                );
            }

            // 위도/경도 범위 검증
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new IllegalArgumentException("위도/경도 값이 유효한 범위를 벗어났습니다.");
            }
        }

        PartyListRequest request = PartyListRequest.builder()
                .category(category)
                .sortType(sortType)
                .userLat(lat)
                .userLon(lon)
                .build();

        PartyListResponse response = partyService.getPartyList(userId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 파티 상세 조회
     * 회원만 접근 가능 (컨트롤러에서 체크 또는 인터셉터/필터 활용)
     */
    @Operation(
            summary = "파티 상세 조회",
            description = "특정 파티의 상세 정보를 조회합니다. 회원만 접근 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PartyDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파티를 찾을 수 없음",
                    content = @Content
            )
    })
    @GetMapping("/{partyId}")
    public ResponseEntity<PartyDetailResponse> getPartyDetail(
            @PathVariable Long partyId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon
    ) {
        PartyDetailResponse response = partyService.getPartyDetail(partyId, userId,userLat,userLon);
        return ResponseEntity.ok(response);
    }

    /**
     * 파티 생성
     */
    @Operation(
            summary = "파티 생성",
            description = "새로운 파티를 생성합니다. 회원만 생성 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "생성 성공, 파티 ID 반환",
                    content = @Content(schema = @Schema(implementation = Long.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<Long> createParty(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PartyCreateRequest request) {

        Long partyId = partyService.createParty(userId, request);

        return ResponseEntity.ok(partyId);
    }

    /**
     * 파티 수정
     */
    @Operation(
            summary = "파티 수정",
            description = """
            파티 정보를 수정합니다.
            
            **수정 권한**
            - 파티 호스트만 수정 가능
            
            **수정 가능 범위**
            - 승인된 파티원이 없을 때: 모든 항목 수정 가능
            - 승인된 파티원이 있을 때: 설명, 이미지만 수정 가능 (가격, 인원, 수령 정보 수정 불가)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "파티 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (수정 권한 없음, 유효하지 않은 데이터)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파티를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{partyId}")
    public ResponseEntity<Void> updateParty(
            @PathVariable Long partyId,
            @AuthenticationPrincipal Long userId,
            @RequestBody PartyUpdateRequest request) {

        partyService.updateParty(partyId, userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 파티 삭제
     */
     @Operation(
            summary = "파티 삭제",
            description = """
            파티를 삭제합니다.
            
            **삭제 권한**
            - 파티 호스트만 삭제 가능
            
            **삭제 제한**
            - 승인된 파티원이 있는 경우 삭제 불가능
            - 승인된 파티원이 없을 때만 삭제 가능
            
            **삭제 시 처리**
            - 관련 채팅방 비활성화
            - 대기 중인 참가 신청 모두 삭제
            """
     )
     @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "파티 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (삭제 권한 없음, 승인된 파티원 존재)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파티를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{partyId}")
    public ResponseEntity<Void> deleteParty(
            @PathVariable Long partyId,
            @AuthenticationPrincipal Long userId) {

        partyService.deleteParty(partyId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "파티 검색",
            description = """
                    q 문자열을 포함하는 제목을 가진 파티를 검색합니다. <br>
                    slice 페이지 처리를 위해, 조회할 다음 페이지가 있는지 체크하는 hasNext 필드와, <br>
                    몇 번째 페이지 인지 (page), 한 페이지 당 몇 개의 파티를 조회할 지 (size) 파라미터로 입력해주시면 됩니다.
                    """
    )
    @GetMapping("/search")
    public APIResponse<PartyQueryListResponse> getParty(
            @RequestParam String q,
            @Parameter(
                    description = "파티 카테고리",
                    example = "ALL",
                    schema = @Schema(allowableValues = {"ALL", "DELIVERY", "GROCERY", "HOUSEHOLD"})
            )
            @RequestParam(defaultValue = "ALL") PartyCategory category,
            @RequestParam(required = false, name = "lat") Double userLat,
            @RequestParam(required = false, name = "lon") Double userLon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return success(partySearchService.searchParty(q, category, page, size, userLat, userLon));
    }

    @Operation(
            summary = "최근 검색어 조회",
            description = """
                    검색 돋보기 클릭 시 보이는 최근 검색어를 조회합니다. <br>
                    한 번에 20개가 조회됩니다.
                    """
    )
    @GetMapping("/search/log")
    public APIResponse<List<String>> getRecentLog() {
         return success(partySearchService.getLog());
    }

    @Operation(
            summary = "특정 최근 검색어 삭제",
            description = """
                    최근 검색어에서 특정 검색어를 삭제합니다. <br>
                    이때 검색어에 대한 Id값은 없고, 최근 검색어 자체를 keyword에 넣어주시면 됩니다.
                    """
    )
    @DeleteMapping("/search/log/{keyword}")
    public APIResponse<?>  deleteRecentLog(@PathVariable String keyword) {
        partySearchService.deleteLog(keyword);
         return success();
    }

    @Operation(
            summary = "모든 최근 검색어 삭제",
            description = """
                    특정 유저에 대한 모든 최근 검색어를 삭제합니다.
                    """
    )
    @DeleteMapping("/search/log")
    public APIResponse<?> deleteRecentLogAll() {
         partySearchService.deleteAllLog();
         return success();
    }
}