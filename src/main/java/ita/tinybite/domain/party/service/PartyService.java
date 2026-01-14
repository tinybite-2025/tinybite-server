package ita.tinybite.domain.party.service;

import ita.tinybite.domain.chat.entity.ChatMessage;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.chat.repository.ChatMessageRepository;
import ita.tinybite.domain.chat.repository.ChatRoomRepository;
import ita.tinybite.domain.chat.service.ChatService;
import ita.tinybite.domain.notification.service.facade.NotificationFacade;
import ita.tinybite.domain.party.dto.request.PartyCreateRequest;
import ita.tinybite.domain.party.dto.request.PartyListRequest;
import ita.tinybite.domain.party.dto.request.PartyUpdateRequest;
import ita.tinybite.domain.party.dto.response.*;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.entity.PickupLocation;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.enums.PartySortType;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.party.repository.PartyRepository;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.location.LocationService;
import ita.tinybite.global.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;
    @Value("${default.image.thumbnail.delivery}")
    private String defaultDeliveryImage;
    @Value("${default.image.thumbnail.grocery}")
    private String defaultGroceryImage;
    @Value("${default.image.thumbnail.household}")
    private String defaultHouseholdImage;
    @Value("${default.image.detail.delivery}")
    private String defaultDeliveryDetailImage;
    @Value("${default.image.detail.grocery}")
    private String defaultGroceryDetailImage;
    @Value("${default.image.detail.household}")
    private String defaultHouseholdDetailImage;
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final PartyParticipantRepository partyParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PartyParticipantRepository participantRepository;
    private final NotificationFacade notificationFacade;

    /**
     * 파티 생성
     */
    @Transactional
    public Long createParty(Long userId, PartyCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        String myTown = getMyTown(request.getPickupLocation().getPickupLatitude(), request.getPickupLocation().getPickupLongitude());

        // 카테고리별 유효성 검증
        validateProductLink(request.getCategory(), request.getProductLink());

        Party party = Party.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .price(request.getTotalPrice())
                .maxParticipants(request.getMaxParticipants())
                .town(myTown)
                .pickupLocation(PickupLocation.builder()
                        .place(request.getPickupLocation().getPlace())
                        .pickupLatitude(request.getPickupLocation().getPickupLatitude())
                        .pickupLongitude(request.getPickupLocation().getPickupLongitude())
                        .build())
                .images(getImagesIfPresent(request.getImages()))
                .thumbnailImage(getThumbnailIfPresent(request.getImages(), request.getCategory()))
                .thumbnailImageDetail(getThumbnailDetailIfPresent(request.getImages(), request.getCategory()))
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

        // 파티가 생성되었다는 메시지를 그룹 채팅방에 저장
        chatService.saveSystemMessage(chatRoom, "파티가 생성되었습니다.");

        return savedParty.getId();
    }

    /**
     * 파티 목록 조회 (홈 화면)
     */
    public PartyListResponse getPartyList(Long userId, PartyListRequest request) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // 페이지네이션 파라미터 (기본값: page=0, size=20)
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;

//        String myTown = getMyTown(request.getUserLat(),request.getUserLon());

        // 동네 기준으로 파티 조회
        List<Party> parties = fetchPartiesByTown(user, request);

        // PartyCardResponse로 변환
        List<PartyCardResponse> cardResponses = parties.stream()
                .map(party -> {
                    // 위치 정보가 있으면 항상 거리 계산
                    if (request.getUserLat() != null && request.getUserLon() != null
                            && party.getPickupLocation() != null) {
                        double distance = DistanceCalculator.calculateDistance(
                                request.getUserLat(),
                                request.getUserLon(),
                                party.getPickupLocation().getPickupLatitude(),
                                party.getPickupLocation().getPickupLongitude()
                        );
                        return convertToCardResponseWithDistance(party, distance);
                    }
                    return convertToCardResponse(party, party.getCreatedAt());
                })
                .toList();


        // 진행 중 파티 정렬
        List<PartyCardResponse> activeParties = cardResponses.stream()
                .filter(p -> !p.getIsClosed())
                .sorted(getComparator(request.getSortType()))
                .toList();

        // 마감된 파티 정렬
        List<PartyCardResponse> closedParties = cardResponses.stream()
                .filter(PartyCardResponse::getIsClosed)
                .sorted(getComparator(request.getSortType()))
                .toList();

        // 진행 중 + 마감된 파티 합치기 (진행 중이 먼저)
        List<PartyCardResponse> allParties = new ArrayList<>();
        allParties.addAll(activeParties);
        allParties.addAll(closedParties);

        // 페이지네이션 적용
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, allParties.size());

        List<PartyCardResponse> paginatedParties = allParties.subList(
                Math.min(startIndex, allParties.size()),
                endIndex
        );

        // hasNext 계산
        boolean hasNext = endIndex < allParties.size();

        // 페이지네이션된 결과를 다시 진행 중/마감으로 분리
        List<PartyCardResponse> paginatedActiveParties = paginatedParties.stream()
                .filter(p -> !p.getIsClosed())
                .collect(Collectors.toList());

        List<PartyCardResponse> paginatedClosedParties = paginatedParties.stream()
                .filter(PartyCardResponse::getIsClosed)
                .collect(Collectors.toList());

        return PartyListResponse.builder()
                .activeParties(paginatedActiveParties)
                .closedParties(paginatedClosedParties)
                .totalCount(allParties.size())
                .hasNext(hasNext)
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

        // 즉시 알림 발송
        notificationFacade.notifyNewPartyRequest(
            party.getHost().getUserId(), // 파티장 ID
            userId,                      // 신청자 ID
            partyId
        );

        // 리마인드 등록
        notificationFacade.reservePartyRequestReminder(
            party.getHost().getUserId(),
            userId,
            partyId
        );

        return oneToOneChatRoom.getId();
    }

    /**
     * 파티 탈퇴 - 인원 감소 시 다시 모집 중으로 변경
     */
    public void leaveParty(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다."));

        PartyParticipant member = partyParticipantRepository
                .findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("파티에 참가하지 않은 사용자입니다."));

        partyParticipantRepository.delete(member);

        // 파티 현재 참여자 수 감소
        party.decrementParticipants();

        // 모집 완료 상태였다면 다시 모집 중으로 변경
        if (party.getStatus() == PartyStatus.COMPLETED) {
            party.changePartyStatus(PartyStatus.RECRUITING);
            partyRepository.save(party);
        }
    }


    public void completeRecruitment(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("파티를 찾을 수 없습니다."));

        // 파티장 권한 확인
        if (!party.getHost().getUserId().equals(userId)) {
            throw new IllegalStateException("파티장만 승인할 수 있습니다");
        }

        if (party.getStatus() != PartyStatus.RECRUITING) {
            throw new IllegalStateException("모집 중인 파티만 완료 처리할 수 있습니다.");
        }

        party.changePartyStatus(PartyStatus.COMPLETED);
        partyRepository.save(party);
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

    private PartyCardResponse convertToCardResponse(Party party,
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

        return PartyDetailResponse.builder()
                .partyId(party.getId())
                .title(party.getTitle())
                .category(party.getCategory())
                .timeAgo(party.getTimeAgo())
                .town(party.getTown())
                .host(HostInfo.builder()
                        .userId(party.getHost().getUserId())
                        .nickname(party.getHost().getNickname())
                        .profileImage(party.getHost().getProfileImage())
                        .hostLocation(party.getHost().getLocation())
                        .build())
                .pickupLocation(party.getPickupLocation())
                .thumbnailImageDetail(party.getThumbnailImageDetail())
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

    @Transactional
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

            // 주어진 좌표로 법정동 반환
            String location = locationService.getLocation(request.getPickupLocation().getPickupLatitude().toString(), request.getPickupLocation().getPickupLongitude().toString());
            // place는 pickupLocation, locaton은 town에 저장
            party.updatePartyLocation(request.getPickupLocation(), location);
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
                .countByPartyIdAndStatusAndUser_UserIdNot(
                        partyId,
                        ParticipantStatus.APPROVED,
                        userId
                );

        if (approvedParticipantsExcludingHost > 0) {
            throw new IllegalStateException("승인된 파티원이 있어 삭제할 수 없습니다");
        }

         chatRoomRepository.deleteByPartyIdAndType(partyId, ChatRoomType.GROUP);
        
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

        // 리마인드 예약 삭제
        notificationFacade.cancelPendingApprovalReminder(partyId, participant.getUser().getUserId());

        // 승인 알림
        notificationFacade.notifyApproval(
            participant.getUser().getUserId(),
            partyId
        );

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

        // 리마인드 예약 삭제
        notificationFacade.cancelPendingApprovalReminder(partyId, participant.getUser().getUserId());

        notificationFacade.notifyRejection(
            participant.getUser().getUserId(),
            partyId
        );
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

        // 알림 대상
        List<Long> memberIds = partyParticipantRepository.findAllByPartyAndStatus(party, ParticipantStatus.APPROVED)
            .stream()
            .map(p -> p.getUser().getUserId())
            .toList();

        // 파티 종료 알림
        notificationFacade.notifyPartyComplete(memberIds, party.getId());
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

        // 신청자만 추가 (호스트는 chatRoom -> party -> host로 조회하기)
        saved.addMember(applicant);

        chatService.saveSystemMessage(chatRoom, "일대일 채팅이 생성되었습니다.");

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

    // ??
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
        return switch (category) {
            case DELIVERY -> defaultDeliveryImage;
            case GROCERY -> defaultGroceryImage;
            case HOUSEHOLD -> defaultHouseholdImage;
            default -> throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + category);
        };
    }

    private String getThumbnailDetailIfPresent(List<String> images, PartyCategory category) {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return switch (category) {
            case DELIVERY -> defaultDeliveryDetailImage;
            case GROCERY -> defaultGroceryDetailImage;
            case HOUSEHOLD -> defaultHouseholdDetailImage;
            default -> throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + category);
        };
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

    //카테고리에 따라 파티 조회
    private List<Party> fetchPartiesByTown(User user, PartyListRequest request) {
        if (user == null || user.getLocation() == null) {
            return List.of();
        }

        String location = user.getLocation();
        PartyCategory category = request.getCategory();

        if (category == PartyCategory.ALL) {
            return partyRepository.findByTown(location);
        } else {
            return partyRepository.findByTownAndCategory(location, category);
        }
    }

    // 정렬 기준에 따른 Comparator 반환
    private Comparator<PartyCardResponse> getComparator(PartySortType sortType) {
        if (sortType == PartySortType.DISTANCE) {
            // 거리 가까운 순
            return Comparator.comparing(PartyCardResponse::getDistanceKm)
                    .thenComparing((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } else {
            // 최신순 (createdAt 내림차순)
            return (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt());
        }
    }

    // 거리 정보 포함 변환
    private PartyCardResponse convertToCardResponseWithDistance(
            Party party, Double distance) {
        PartyCardResponse response = convertToCardResponse(party, party.getCreatedAt());
        response.addDistanceKm(distance);
        return response;
    }

    private String getMyTown(Double pickupLatitude, Double pickupLongitude) {
        return locationService.getLocation(Double.toString(pickupLatitude), Double.toString(pickupLongitude));
    }
}

