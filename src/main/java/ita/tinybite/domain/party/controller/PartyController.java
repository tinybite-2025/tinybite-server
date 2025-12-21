package ita.tinybite.domain.party.controller;

import ita.tinybite.domain.party.dto.request.PartyCreateRequest;
import ita.tinybite.domain.party.dto.response.PartyDetailResponse;
import ita.tinybite.domain.party.dto.response.PartyListResponse;
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

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    /**
     * 파티 목록 조회 (홈 화면)
     * 비회원도 접근 가능
     */
    @GetMapping
    public ResponseEntity<PartyListResponse> getPartyList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "ALL") PartyCategory category,
            @RequestParam String latitude,
            @RequestParam String longitude) {

        Long userId = extractUserIdFromToken(token); // JWT에서 userId 추출

        PartyListResponse response = partyService.getPartyList(
                userId, category, latitude, longitude);

        return ResponseEntity.ok(response);
    }

    /**
     * 파티 상세 조회
     * 회원만 접근 가능 (컨트롤러에서 체크 또는 인터셉터/필터 활용)
     */
    @GetMapping("/{partyId}")
    public ResponseEntity<PartyDetailResponse> getPartyDetail(
            @PathVariable Long partyId,
            @RequestHeader("Authorization") String token,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {

        Long userId = extractUserIdFromToken(token);

        PartyDetailResponse response = partyService.getPartyDetail(partyId, userId, latitude, longitude);
        return ResponseEntity.ok(response);
    }

    /**
     * 파티 생성
     */
    @PostMapping
    public ResponseEntity<Long> createParty(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PartyCreateRequest request) {

        Long userId = extractUserIdFromToken(token);
        Long partyId = partyService.createParty(userId, request);

        return ResponseEntity.ok(partyId);
    }

    /**
     * 파티 참여
     */
    @PostMapping("/{partyId}/join")
    public ResponseEntity<Void> joinParty(
            @PathVariable Long partyId,
            @RequestHeader("Authorization") String token) {

        Long userId = extractUserIdFromToken(token);
        partyService.joinParty(partyId, userId);

        return ResponseEntity.ok().build();
    }

    private Long extractUserIdFromToken(String token) {
        // JWT 토큰에서 userId 추출 로직
        // 실제 구현은 JWT 유틸 클래스 활용
        return null; // TODO: 실제 구현 필요
    }
}