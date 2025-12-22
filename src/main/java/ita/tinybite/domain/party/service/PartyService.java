package ita.tinybite.domain.party.service;

import ita.tinybite.domain.party.dto.request.PartyCreateRequest;
import ita.tinybite.domain.party.dto.request.PartyUpdateRequest;
import ita.tinybite.domain.party.dto.response.*;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.entity.PickupLocation;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.party.repository.PartyRepository;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ita.tinybite.global.location.LocationService;
import ita.tinybite.global.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final PartyParticipantRepository partyParticipantRepository;

    /**
     * 파티 생성
     */
    @Transactional
    public Long createParty(Long userId, PartyCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 카테고리별 유효성 검증
        validateProductLink(request.getCategory(), request.getProductLink());

        // 첫 번째 이미지를 썸네일로 사용, 없으면 기본 이미지
        String thumbnailImage = getDefaultImageIfEmpty(request.getImages(), request.getCategory());

        Party party = Party.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .price(request.getTotalPrice())
                .maxParticipants(request.getMaxParticipants())
                .pickupLocation(request.getPickupLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .thumbnailImage(thumbnailImage)
                .link(request.getProductLink())
                .description(request.getDescription())
                .isClosed(false)
                .host(user)
                .build();

        Party savedParty = partyRepository.save(party);
        return savedParty.getId();
    }

    /**
     * 파티 목록 조회 (홈 화면)
     */
    public PartyListResponse getPartyList(Long userId, PartyCategory category,
                                          String userLat, String userLon) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // 동네 기준으로 파티 조회
        List<Party> parties;
        if (user != null && user.getLocation() != null) {
            if (category == PartyCategory.ALL) {
                parties = partyRepository.findByLocation(user.getLocation());
            } else {
                parties = partyRepository.findByLocationAndCategory(
                        user.getLocation(), category);
            }
        } else {
            // 비회원이거나 동네 미설정 시
            String location = locationService.getLocation(userLat,userLon);
            if (category == PartyCategory.ALL) {
                parties = partyRepository.findByLocation(location);
            } else {
                parties = partyRepository.findByLocationAndCategory(
                        location, category);
            }
        }
        List<PartyCardResponse> cardResponses = parties.stream()
                .map(party -> {
                    // DistanceCalculator 활용
                    double distance = DistanceCalculator.calculateDistance(
                            Double.parseDouble(userLat), Double.parseDouble(userLon),
                            party.getLatitude(), party.getLongitude()
                    );
                    return convertToCardResponse(party, distance, userId, party.getCreatedAt());
                })
                .collect(Collectors.toList());

        // 진행 중 파티: 거리 가까운 순 정렬
        List<PartyCardResponse> activeParties = cardResponses.stream()
                .filter(p -> !p.getIsClosed())
                .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
                .collect(Collectors.toList());

        // 마감된 파티: 거리 가까운 순 정렬
        List<PartyCardResponse> closedParties = cardResponses.stream()
                .filter(PartyCardResponse::getIsClosed)
                .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
                .collect(Collectors.toList());

        return PartyListResponse.builder()
                .activeParties(activeParties)
                .closedParties(closedParties)
                .totalCount(parties.size())
                .hasNext(false)
                .build();
    }

    /**
     * 파티 상세 조회
     */
    public PartyDetailResponse getPartyDetail(Long partyId, Long userId, Double userLat, Double userLon) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // 현재 사용자가 참여 중인지 확인
        boolean isParticipating = false;
        if (user != null) {
            isParticipating = partyParticipantRepository
                    .existsByPartyAndUserAndIsApprovedTrue(party, user);
        }

        // 거리 계산 (사용자 위치 필요)
        double distance = 0.0;
        if (user != null) {
            distance = DistanceCalculator.calculateDistance(
                    userLat,
                    userLon,
                    party.getLatitude(),
                    party.getLongitude()
            );
        }

        return convertToDetailResponse(party, distance, isParticipating);
    }

    /**
     * 파티 참여
     */
    @Transactional
    public void joinParty(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 마감 체크
        if (party.getIsClosed()) {
            throw new IllegalStateException("마감된 파티입니다");
        }

        // 인원 체크
        if (party.getApprovedParticipantCount() >= party.getMaxParticipants()) {
            throw new IllegalStateException("인원이 가득 찼습니다");
        }

        // 중복 참여 체크
        if (partyParticipantRepository.existsByPartyAndUser(party, user)) {
            throw new IllegalStateException("이미 참여 신청한 파티입니다");
        }

        // 참여 신청 (승인 대기)
        PartyParticipant participant = PartyParticipant.builder()
                .party(party)
                .user(user)
                .isApproved(false) // 초기에는 승인 대기
                .build();

        partyParticipantRepository.save(participant);
    }

    private void validateProductLink(PartyCategory category, String productLink) {
        // 배달은 링크 불가
        if (category == PartyCategory.DELIVERY && productLink != null) {
            throw new IllegalArgumentException("배달 파티는 상품 링크를 추가할 수 없습니다");
        }
    }

    private String getDefaultImageIfEmpty(List<String> images, PartyCategory category) {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }

        // 카테고리별 기본 이미지
        return switch (category) {
            case DELIVERY -> "/images/default-delivery.png";
            case GROCERY -> "/images/default-grocery.png";
            case HOUSEHOLD -> "/images/default-household.png";
            default -> "/images/default-party.png";
        };
    }

    private PartyCardResponse convertToCardResponse(Party party, double distanceKm, Long userId,
                                                    java.time.LocalDateTime createdAt) {
        int pricePerPerson = party.getPrice() / party.getMaxParticipants();
        String participantStatus = party.getApprovedParticipantCount() + "/"
                + party.getMaxParticipants() + "명";

        return PartyCardResponse.builder()
                .partyId(party.getId())
                .thumbnailImage(party.getThumbnailImage())
                .title(party.getTitle())
                .pricePerPerson(pricePerPerson)
                .participantStatus(participantStatus)
                .distance(DistanceCalculator.formatDistance(distanceKm))
                .distanceKm(distanceKm)
                .timeAgo(party.getTimeAgo())
                .isClosed(party.getIsClosed())
                .category(party.getCategory())
                .createdAt(createdAt)
                .build();
    }


    private PartyDetailResponse convertToDetailResponse(Party party, double distance,
                                                        boolean isParticipating) {
        int currentCount = party.getApprovedParticipantCount();
        int pricePerPerson = party.getPrice() / party.getMaxParticipants();

        // 이미지 파싱
        List<String> images = new ArrayList<>();
        if (party.getImage() != null && !party.getImage().isEmpty()) {
            images = List.of(party.getImage().split(","));
        }

        return PartyDetailResponse.builder()
                .partyId(party.getId())
                .title(party.getTitle())
                .category(party.getCategory())
                .timeAgo(party.getTimeAgo())
                .host(HostInfo.builder()
                        .userId(party.getHost().getUserId())
                        .nickname(party.getHost().getNickname())
                        .profileImage(party.getHost().getProfileImage())
                        .neighborhood(party.getNeighborhood().getName())
                        .build())
                .pickupLocation(party.getPickupLocation())
                .distance(DistanceCalculator.formatDistance(distance))
                .currentParticipants(currentCount)
                .maxParticipants(party.getMaxParticipants())
                .remainingSlots(party.getMaxParticipants() - currentCount)
                .pricePerPerson(pricePerPerson)
                .totalPrice(party.getPrice())
                .productLink(party.getLink() != null ?
                        ProductLink.builder()
                                .url(party.getLink())
                                .build() : null)
                .description(party.getDescription())
                .images(images)
                .isClosed(party.getIsClosed())
                .isParticipating(isParticipating)
                .build();
    }

    @Transactional
    public void updateParty(Long partyId, Long userId, PartyUpdateRequest request) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 파티장 권한 확인
        if (!party.getHost().getUserId().equals(userId)) {
            throw new IllegalStateException("파티장만 수정할 수 있습니다");
        }

        // 승인된 파티원 확인
        boolean hasApprovedParticipants = party.getApprovedParticipantCount() > 1;

        if (hasApprovedParticipants) {
            // 승인된 파티원이 있는 경우: 설명과 이미지만 수정 가능
            party.updateLimitedFields(
                    request.getDescription(),
                    request.getImages()
            );
        } else {
            // 승인된 파티원이 없는 경우: 모든 항목 수정 가능
            party.updateAllFields(
                    request.getTitle(),
                    request.getTotalPrice(),
                    request.getMaxParticipants(),
                    new PickupLocation(request.getPickupLocation(),request.getLatitude(),request.getLongitude()),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getProductLink(),
                    request.getDescription(),
                    request.getImages()
            );
        }
    }

    @Transactional
    public void deleteParty(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 파티장 권한 확인
        if (!party.getHost().getUserId().equals(userId)) {
            throw new IllegalStateException("파티장만 삭제할 수 있습니다");
        }

        // 승인된 파티원 확인
        boolean hasApprovedParticipants = party.getApprovedParticipantCount() > 1;

        if (hasApprovedParticipants) {
            throw new IllegalStateException("승인된 파티원이 있어 삭제할 수 없습니다");
        }

        // 삭제 실행
        partyRepository.delete(party);
    }

}

