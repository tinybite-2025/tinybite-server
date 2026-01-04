package ita.tinybite.domain.party.service;

import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.chat.repository.ChatRoomRepository;
import ita.tinybite.domain.party.dto.request.PartyCreateRequest;
import ita.tinybite.domain.party.dto.request.PartyQueryListResponse;
import ita.tinybite.domain.party.dto.request.PartyUpdateRequest;
import ita.tinybite.domain.party.dto.response.*;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.entity.PickupLocation;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.party.repository.PartyRepository;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ita.tinybite.global.location.LocationService;
import ita.tinybite.global.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final ChatRoomRepository chatRoomRepository;
    private final PartyParticipantRepository participantRepository;
    /**
     * 파티 생성
     */
    @Transactional
    public Long createParty(Long userId, PartyCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 카테고리별 유효성 검증
        validateProductLink(request.getCategory(), request.getProductLink());

        Party party = Party.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .price(request.getTotalPrice())
                .maxParticipants(request.getMaxParticipants())
                .pickupLocation(PickupLocation.builder()
                        .place(request.getPickupLocation().getPlace())
                        .pickupLatitude(request.getPickupLocation().getPickupLatitude())
                        .pickupLongitude(request.getPickupLocation().getPickupLongitude())
                        .build())
                .images(getImagesIfPresent(request.getImages()))
                .thumbnailImage(getThumbnailIfPresent(request.getImages(), request.getCategory()))
                .link(getLinkIfValid(request.getProductLink(), request.getCategory()))
                .description(getDescriptionIfPresent(request.getDescription()))
                .currentParticipants(1)
                .status(PartyStatus.RECRUITING)
                .isClosed(false)
                .host(user)
                .build();

        Party savedParty = partyRepository.save(party);

        // 단체 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .party(savedParty)
                .type(ChatRoomType.GROUP)
                .name(savedParty.getTitle())
                .isActive(true)
                .build();

        // 파티 생성자(호스트)를 채팅방 멤버로 추가
        chatRoom.addMember(user);

        chatRoomRepository.save(chatRoom);

        // Participant 생성
        PartyParticipant participant = PartyParticipant.builder()
                .party(savedParty)
                .user(user)
                .status(ParticipantStatus.APPROVED)
                .isApproved(true)
                .joinedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);
        return savedParty.getId();
    }

    /**
     * 파티 목록 조회 (홈 화면)
     */
    public PartyListResponse getPartyList(Long userId, PartyCategory category) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // 동네 기준으로 파티 조회
        List<Party> parties = List.of();
        if (user != null && user.getLocation() != null) {
            if (category == PartyCategory.ALL) {
                parties = partyRepository.findByPickupLocation_Place(user.getLocation());
            } else {
                parties = partyRepository.findByPickupLocation_PlaceAndCategory(
                        user.getLocation(), category);
            }
        }
//          else {
//            // 비회원이거나 동네 미설정 시
//            String location = locationService.getLocation(userLat, userLon);
//            if (category == PartyCategory.ALL) {
//                parties = partyRepository.findByPickupLocation_Place(location);
//            } else {
//                parties = partyRepository.findByPickupLocation_PlaceAndCategory(
//                        location, category);
//            }
//        }

//        List<PartyCardResponse> cardResponses = parties.stream()
//                .map(party -> {
//                    // DistanceCalculator 활용
//                    double distance = DistanceCalculator.calculateDistance(
//                            Double.parseDouble(userLat), Double.parseDouble(userLon),
//                            party.getLatitude(), party.getLongitude()
//                    );
//                    return convertToCardResponse(party, distance, userId, party.getCreatedAt());
//                })
//                .collect(Collectors.toList());
//
//        // 진행 중 파티: 거리 가까운 순 정렬
//        List<PartyCardResponse> activeParties = cardResponses.stream()
//                .filter(p -> !p.getIsClosed())
//                .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
//                .collect(Collectors.toList());
//
//        // 마감된 파티: 거리 가까운 순 정렬
//        List<PartyCardResponse> closedParties = cardResponses.stream()
//                .filter(PartyCardResponse::getIsClosed)
//                .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
//                .collect(Collectors.toList());


        List<PartyCardResponse> cardResponses = parties.stream()
                .map(party -> convertToCardResponse(party, userId, party.getCreatedAt()))
                .collect(Collectors.toList());

        // 진행 중 파티: 최신순 정렬 (createdAt 기준 내림차순)
                List<PartyCardResponse> activeParties = cardResponses.stream()
                        .filter(p -> !p.getIsClosed())
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

        // 마감된 파티: 최신순 정렬 (createdAt 기준 내림차순)
                List<PartyCardResponse> closedParties = cardResponses.stream()
                        .filter(PartyCardResponse::getIsClosed)
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
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
                    .existsByPartyAndUserAndStatus(party, user, ParticipantStatus.APPROVED);
        }

        // 거리 계산 (사용자 위치 필요)
        double distance = 0.0;
        if (validateLocation(user,userLat, userLon,party)) {
            distance = DistanceCalculator.calculateDistance(
                    userLat,
                    userLon,
                    party.getPickupLocation().getPickupLatitude(),
                    party.getPickupLocation().getPickupLongitude()
            );
        }

        return convertToDetailResponse(party, distance, isParticipating);
    }

    private boolean validateLocation(User user, Double userLat, Double userLon, Party party) {
        return (user != null
                && userLat != null
                && userLon!= null
                && party.getPickupLocation().getPickupLatitude()!= null
                && party.getPickupLocation().getPickupLongitude()!=null);
    }

    /**
     * 파티 참여
     */
    @Transactional
    public Long joinParty(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 유효성 검증
        validateJoinRequest(party, user);

        // 1:1 채팅방 생성 (파티장 + 신청자)
        ChatRoom oneToOneChatRoom = createOneToOneChatRoom(party, user);

        // 참여 신청 생성
        PartyParticipant participant = PartyParticipant.builder()
                .party(party)
                .user(user)
                .status(ParticipantStatus.PENDING)
                .oneToOneChatRoom(oneToOneChatRoom)
                .build();

        PartyParticipant saved = partyParticipantRepository.save(participant);

        return saved.getId();
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

    private PartyCardResponse convertToCardResponse(Party party, Long userId,
                                                    LocalDateTime createdAt) {
        int pricePerPerson = party.getPrice() / party.getMaxParticipants();
        String participantStatus = party.getCurrentParticipants() + "/"
                + party.getMaxParticipants() + "명";

        return PartyCardResponse.builder()
                .partyId(party.getId())
                .thumbnailImage(party.getThumbnailImage())
                .title(party.getTitle())
                .pricePerPerson(pricePerPerson)
                .participantStatus(participantStatus)
//                .distance(DistanceCalculator.formatDistance(distanceKm))
//                .distanceKm(distanceKm)
                .timeAgo(party.getTimeAgo())
                .isClosed(party.getIsClosed())
                .category(party.getCategory())
                .createdAt(createdAt)
                .build();
    }


    private PartyDetailResponse convertToDetailResponse(Party party, double distance,
                                                        boolean isParticipating) {
        int currentCount = party.getCurrentParticipants();
        int pricePerPerson = party.getPrice() / party.getMaxParticipants();

        // 이미지 파싱
//        List<String> images = new ArrayList<>();
//        if (party.getImages() != null && !party.getImages().isEmpty()) {
//            images = List.of(party.getImages());
//        }

        return PartyDetailResponse.builder()
                .partyId(party.getId())
                .title(party.getTitle())
                .category(party.getCategory())
                .timeAgo(party.getTimeAgo())
                .host(HostInfo.builder()
                        .userId(party.getHost().getUserId())
                        .nickname(party.getHost().getNickname())
                        .profileImage(party.getHost().getProfileImage())
                        .build())
                .pickupLocation(party.getPickupLocation())
                .thumbnailImage(party.getThumbnailImage())
                .distance(formatDistanceIfExists(distance))
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
                .images(party.getImages())
                .isClosed(party.getIsClosed())
                .isParticipating(isParticipating)
                .build();
    }

    public void updateParty(Long partyId, Long userId, PartyUpdateRequest request) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 파티장 권한 확인
        if (!party.getHost().getUserId().equals(userId)) {
            throw new IllegalStateException("파티장만 수정할 수 있습니다");
        }

        // 호스트 제외한 승인된 파티원 수 확인
        int approvedParticipantsExcludingHost = participantRepository
                .countByPartyIdAndStatusAndUser_UserIdNot(
                        partyId,
                        ParticipantStatus.APPROVED,
                        userId
                );

        if (approvedParticipantsExcludingHost > 0) {
            // 다른 승인된 파티원이 있는 경우: 설명과 이미지만 수정 가능
            party.updateLimitedFields(
                    request.getDescription(),
                    request.getImages()
            );
        } else {
            // 승인된 파티원이 없는 경우, 호스트 혼자인 경우: 모든 항목 수정 가능
            party.updateAllFields(
                    request.getTitle(),
                    request.getTotalPrice(),
                    request.getMaxParticipants(),
                    getPickUpLocationIfExists(request, party),
                    request.getProductLink(),
                    request.getDescription(),
                    request.getImages()
            );
        }
    }
    private PickupLocation getPickUpLocationIfExists(PartyUpdateRequest request, Party currentParty) {
        if (request.getPickupLocation() == null) {
            return currentParty.getPickupLocation();
        }
        PickupLocation requestPickup = request.getPickupLocation();
        PickupLocation currentPickup = currentParty.getPickupLocation();

        // 각 필드별로 새 값이 있으면 사용, 없으면 기존 값 유지
        String place = requestPickup.getPlace() != null
                ? requestPickup.getPlace()
                : (currentPickup != null ? currentPickup.getPlace() : "");

        Double latitude = requestPickup.getPickupLatitude() != null
                ? requestPickup.getPickupLatitude()
                : (currentPickup != null ? currentPickup.getPickupLatitude() : null);

        Double longitude = requestPickup.getPickupLongitude() != null
                ? requestPickup.getPickupLongitude()
                : (currentPickup != null ? currentPickup.getPickupLongitude() : null);

        return new PickupLocation(place, latitude, longitude);

    }

    @Transactional
    public void deleteParty(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 파티장 권한 확인
        if (!party.getHost().getUserId().equals(userId)) {
            throw new IllegalStateException("파티장만 삭제할 수 있습니다");
        }

        // 호스트 제외한 승인된 파티원 수 확인
        int approvedParticipantsExcludingHost = participantRepository
                .countByPartyIdAndStatusAndUserIdNot(
                        partyId,
                        ParticipantStatus.APPROVED,
                        userId
                );

        if (approvedParticipantsExcludingHost > 0) {
            throw new IllegalStateException("승인된 파티원이 있어 삭제할 수 없습니다");
        }

        // 삭제 실행
        partyRepository.delete(party);
    }


    /**
     * 참여 승인 → 단체 채팅방 자동 입장
     */
    @Transactional
    public void approveParticipant(Long partyId, Long participantId, Long hostId) {
        Party party = partyRepository.findByIdWithHost(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 파티장 권한 확인
        if (!party.getHost().getUserId().equals(hostId)) {
            throw new IllegalStateException("파티장만 승인할 수 있습니다");
        }

        PartyParticipant participant = partyParticipantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여 신청을 찾을 수 없습니다"));


        // 현재 인원이 최대 인원을 초과하는지 검증
        if (party.getCurrentParticipants() >= party.getMaxParticipants()) {
            throw new IllegalStateException("파티 인원이 가득 찼습니다");
        }

        // 승인 처리
        participant.approve();

        // 파티 현재 참여자 수 증가
        party.incrementParticipants();

        // 단체 채팅방 조회 또는 생성
        ChatRoom groupChatRoom = getOrCreateGroupChatRoom(party);

        // 단체 채팅방에 참여자 추가
        groupChatRoom.addMember(participant.getUser());

        // 목표 인원 달성 확인
        checkAndCloseIfFull(party);
    }

    /**
     * 참여 거절
     */
    @Transactional
    public void rejectParticipant(Long partyId, Long participantId, Long hostId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        if (!party.getHost().getUserId().equals(hostId)) {
            throw new IllegalStateException("파티장만 거절할 수 있습니다");
        }

        PartyParticipant participant = partyParticipantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여 신청을 찾을 수 없습니다"));

        // 거절 처리
        participant.reject();

        // 1:1 채팅방 비활성화
        if (participant.getOneToOneChatRoom() != null) {
            participant.getOneToOneChatRoom().deactivate();
        }

    }

    /**
     * 승인 대기 목록 조회
     */
    public List<PartyParticipant> getPendingParticipants(Long partyId, Long hostId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        if (!party.getHost().getUserId().equals(hostId)) {
            throw new IllegalStateException("파티장만 조회할 수 있습니다");
        }

        return partyParticipantRepository.findByPartyAndStatus(party, ParticipantStatus.PENDING);
    }

    /**
     * 단체 채팅방 조회
     */
    public ChatRoomResponse getGroupChatRoom(Long partyId, Long userId) {
        Party party = partyRepository.findByIdWithHost(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 접근 권한 확인
        validateGroupChatRoomAccess(party, userId);

        ChatRoom chatRoom= chatRoomRepository.findByPartyAndType(party, ChatRoomType.GROUP)
                .orElseThrow(() -> new IllegalStateException("단체 채팅방이 아직 생성되지 않았습니다"));

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .type(chatRoom.getType())
                .party(PartyInfo.builder()
                        .id(partyId)
                        .title(party.getTitle())
                        .host(HostInfo.builder()
                                .userId(party.getHost().getUserId())
                                .nickname(party.getHost().getNickname())
                                .profileImage(party.getHost().getProfileImage())
                                .build()
                        ).build()
                ).build();
    }

    /**
     * 1:1 채팅방 조회
     */
    public ChatRoom getOneToOneChatRoom(Long participantId, Long userId) {
        PartyParticipant participant = partyParticipantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여 신청을 찾을 수 없습니다"));

        // 파티장 또는 신청자만 접근 가능
        boolean isHost = participant.getParty().getHost().getUserId().equals(userId);
        boolean isApplicant = participant.getUser().getUserId().equals(userId);

        if (!isHost && !isApplicant) {
            throw new IllegalStateException("1:1 채팅방에 접근할 수 없습니다");
        }

        return participant.getOneToOneChatRoom();
    }

    /**
     * 파티 결산 가능 여부 확인
     */
    public boolean canSettle(Long partyId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        // 목표 인원 달성 여부
        return party.getCurrentParticipants() >= party.getMaxParticipants();
    }

    /**
     * 파티 결산 (마감)
     */
    @Transactional
    public void settleParty(Long partyId, Long hostId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다"));

        if (!party.getHost().getUserId().equals(hostId)) {
            throw new IllegalStateException("파티장만 결산할 수 있습니다");
        }

        if (!canSettle(partyId)) {
            throw new IllegalStateException("목표 인원이 달성되지 않았습니다");
        }

        // 파티 마감
        party.close();
    }

    // ========== Private Methods ==========

    private void validateJoinRequest(Party party, User user) {
        if (party.getIsClosed()) {
            throw new IllegalStateException("마감된 파티입니다");
        }

        if (party.getCurrentParticipants() >= party.getMaxParticipants()) {
            throw new IllegalStateException("인원이 가득 찼습니다");
        }

        if (partyParticipantRepository.existsByPartyAndUser(party, user)) {
            throw new IllegalStateException("이미 참여 신청한 파티입니다");
        }

        if (party.getHost().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("파티장은 참여 신청할 수 없습니다");
        }
    }

    private ChatRoom createOneToOneChatRoom(Party party, User applicant) {
        ChatRoom chatRoom = ChatRoom.builder()
                .party(party)
                .type(ChatRoomType.ONE_TO_ONE)
                .name(party.getTitle())
                .isActive(true)
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        // 파티장과 신청자 추가
        saved.addMember(party.getHost());
        saved.addMember(applicant);

        return saved;
    }

    private ChatRoom getOrCreateGroupChatRoom(Party party) {
        return chatRoomRepository.findByPartyAndType(party, ChatRoomType.GROUP)
                .orElseGet(() -> {
                    ChatRoom chatRoom = ChatRoom.builder()
                            .party(party)
                            .type(ChatRoomType.GROUP)
                            .name(party.getTitle())
                            .isActive(true)
                            .build();

                    ChatRoom saved = chatRoomRepository.save(chatRoom);

                    // 파티장 자동 추가
                    saved.addMember(party.getHost());

                    return saved;
                });
    }

    private void validateGroupChatRoomAccess(Party party, Long userId) {
        boolean isHost = party.getHost().getUserId().equals(userId);
        boolean isApproved = partyParticipantRepository
                .existsByPartyAndUserUserIdAndStatus(party, userId, ParticipantStatus.APPROVED);

        if (!isHost && !isApproved) {
            throw new IllegalStateException("단체 채팅방에 접근할 수 없습니다");
        }
    }

    private void checkAndCloseIfFull(Party party) {
        if (party.getCurrentParticipants() >= party.getMaxParticipants()) {
            party.close();
        }
    }

    // 헬퍼 메서드들
    private List<String> getImagesIfPresent(List<String> images) {
        return (images != null && !images.isEmpty()) ? images : null;
    }

    private String getThumbnailIfPresent(List<String> images, PartyCategory category) {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    private String getLinkIfValid(String link, PartyCategory category) {
        if (link != null && !link.isBlank()) {
            validateProductLink(category, link);
            return link;
        }
        return null;
    }

    private String getDescriptionIfPresent(String description) {
        return (description != null && !description.isBlank()) ? description : null;
    }

    private String formatDistanceIfExists(Double distance) {
        return distance!= null? DistanceCalculator.formatDistance(distance):null;
    }


}

