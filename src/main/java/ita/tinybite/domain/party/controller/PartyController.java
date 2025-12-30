package ita.tinybite.domain.party.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ita.tinybite.domain.auth.entity.JwtTokenProvider;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.party.dto.request.PartyCreateRequest;
import ita.tinybite.domain.party.dto.response.ChatRoomResponse;
import ita.tinybite.domain.party.dto.response.PartyDetailResponse;
import ita.tinybite.domain.party.dto.response.PartyListResponse;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.service.PartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "파티 API", description = "파티 생성, 조회, 참여 관련 API")
@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;
    private final JwtTokenProvider jwtTokenProvider;


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
    public ResponseEntity<Long> joinParty(
            @PathVariable Long partyId,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserId(token);
        partyService.joinParty(partyId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "참여 승인", description = "파티장이 참여를 승인하면 단체 채팅방에 자동 입장됩니다")
    @PostMapping("{partyId}/participants/{participantId}/approve")
    public ResponseEntity<Void> approveParticipant(
            @PathVariable Long partyId,
            @PathVariable Long participantId,
            @RequestHeader("Authorization") String token) {

        Long hostId = jwtTokenProvider.getUserId(token);
        partyService.approveParticipant(partyId, participantId, hostId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "참여 거절", description = "파티장이 참여를 거절합니다")
    @PostMapping("/participants/{participantId}/reject")
    public ResponseEntity<Void> rejectParticipant(
            @PathVariable Long partyId,
            @PathVariable Long participantId,
            @RequestHeader("Authorization") String token) {

        Long hostId = jwtTokenProvider.getUserId(token);
        partyService.rejectParticipant(partyId, participantId, hostId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "단체 채팅방 조회", description = "승인된 참여자가 단체 채팅방을 조회합니다")
    @GetMapping("{partyId}/chat/group")
    public ResponseEntity<ChatRoomResponse> getGroupChatRoom(
            @PathVariable Long partyId,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserId(token);
        ChatRoomResponse chatRoom = partyService.getGroupChatRoom(partyId, userId);

        return ResponseEntity.ok(chatRoom);
    }

    @Operation(summary = "결산 가능 여부", description = "목표 인원 달성 시 true 반환")
    @GetMapping("/can-settle")
    public ResponseEntity<Boolean> canSettle(@PathVariable Long partyId) {
        boolean canSettle = partyService.canSettle(partyId);
        return ResponseEntity.ok(canSettle);
    }

    @Operation(summary = "파티 결산", description = "목표 인원 달성 후 파티를 마감합니다")
    @PostMapping("/settle")
    public ResponseEntity<Void> settleParty(
            @PathVariable Long partyId,
            @RequestHeader("Authorization") String token) {

        Long hostId = jwtTokenProvider.getUserId(token);
        partyService.settleParty(partyId, hostId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "승인 대기 목록", description = "파티장이 승인 대기 중인 참여자 목록을 조회합니다")
    @GetMapping("/participants/pending")
    public ResponseEntity<List<PartyParticipant>> getPendingParticipants(
            @PathVariable Long partyId,
            @RequestHeader("Authorization") String token) {

        Long hostId = jwtTokenProvider.getUserId(token);
        List<PartyParticipant> participants = partyService.getPendingParticipants(partyId, hostId);

        return ResponseEntity.ok(participants);
    }



    /**
     * 파티 목록 조회 (홈 화면)
     */
    @GetMapping
    public ResponseEntity<PartyListResponse> getPartyList(
            @Parameter(description = "JWT 토큰", required = false)
            @RequestHeader(value = "Authorization", required = false) String token,

            @Parameter(
                    description = "파티 카테고리",
                    example = "ALL",
                    schema = @Schema(allowableValues = {"ALL", "DELIVERY", "GROCERY", "HOUSEHOLD"})
            )
            @RequestParam(defaultValue = "ALL") PartyCategory category,

            @Parameter(description = "사용자 위도", required = true, example = "37.4979")
            @RequestParam String latitude,

            @Parameter(description = "사용자 경도", required = true, example = "127.0276")
            @RequestParam String longitude) {
        Long userId = jwtTokenProvider.getUserId(token);

        PartyListResponse response = partyService.getPartyList(
                userId, category, latitude, longitude);

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
            @RequestHeader("Authorization") String token,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        Long userId = jwtTokenProvider.getUserId(token);

        PartyDetailResponse response = partyService.getPartyDetail(partyId, userId, latitude, longitude);
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
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PartyCreateRequest request) {

        Long userId = jwtTokenProvider.getUserId(token);
        Long partyId = partyService.createParty(userId, request);

        return ResponseEntity.ok(partyId);
    }

}