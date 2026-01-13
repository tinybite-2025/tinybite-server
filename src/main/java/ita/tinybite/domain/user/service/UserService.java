package ita.tinybite.domain.user.service;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.party.dto.response.PartyCardResponse;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.party.repository.PartyRepository;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.domain.user.dto.res.RejoinValidationResponse;
import ita.tinybite.domain.user.dto.res.UserResDto;
import ita.tinybite.domain.user.dto.res.WithDrawValidationResponse;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.entity.WithDrawUser;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.domain.user.repository.WithDrawUserRepository;
import ita.tinybite.global.exception.ActivePartyExistsException;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.AuthErrorCode;
import ita.tinybite.global.exception.errorcode.UserErrorCode;
import ita.tinybite.global.location.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final SecurityProvider securityProvider;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final WithDrawUserRepository withDrawUserRepository;
    private final PartyParticipantRepository participantRepository;
    private final PartyRepository partyRepository;

    public UserResDto getUser() {
        User user = securityProvider.getCurrentUser();
        return UserResDto.of(user);
    }

    @Transactional
    public void updateUser(UpdateUserReqDto req) {
        User user = securityProvider.getCurrentUser();
        if(req.nickname() != null) user.update(req);
    }

    @Transactional
    public void updateLocation(String latitude, String longitude) {
        User user = securityProvider.getCurrentUser();
        String location = locationService.getLocation(latitude, longitude);
        user.updateLocation(location);
    }

    /**
     * 회원 탈퇴 처리
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Error("해당하는 유저가 없습니다"));

        // 1. 탈퇴 가능 여부 검증
        WithDrawValidationResponse validation = validateWithdrawal(userId);
        if (!validation.isCanWithdraw()) {
            throw new ActivePartyExistsException(
                    "진행 중인 파티가 " + validation.getActivePartyCount() + "개 있습니다. " +
                            "모든 파티를 종료하거나 나간 후 탈퇴해 주세요."
            );
        }

        // 2. 채팅방에 퇴장 메시지 전송
//        chatRoomService.notifyUserWithdrawal(userId, user.getNickname());

        // 3. 탈퇴 기록 생성 (재가입 제한용)
        WithDrawUser withdrawnUser = WithDrawUser.from(user);
        withDrawUserRepository.save(withdrawnUser);

        // 4. 사용자 정보 익명화
        user.withdraw();
    }


    public List<PartyCardResponse> getActiveParties(Long userId) {
        List<PartyParticipant> participants = participantRepository
                .findActivePartiesByUserId(
                        userId,
                        PartyStatus.RECRUITING,
                        ParticipantStatus.APPROVED
                );

        return participants.stream()
                .map(pp -> {
                    Party party = pp.getParty();
                    int currentParticipants = participantRepository
                            .countByPartyIdAndStatus(party.getId(), ParticipantStatus.APPROVED);
                    return PartyCardResponse.from(party, currentParticipants);
                })
                .collect(Collectors.toList());
    }

    /**
     * 회원 탈퇴 가능 여부 확인
     */
    public WithDrawValidationResponse validateWithdrawal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Error("유저를 찾을 수 없습니다"));

        // 1. 호스트로 있는 활성 파티 확인
        long hostPartyCount = participantRepository.countActivePartiesByHostId(
                userId,
                Arrays.asList(PartyStatus.RECRUITING, PartyStatus.RECRUITING)
        );

        // 2. 참가자로 있는 활성 파티 확인
        long participantPartyCount = participantRepository.countActivePartiesByUserId(
                userId,
                Arrays.asList(PartyStatus.RECRUITING, PartyStatus.RECRUITING),
                ParticipantStatus.APPROVED
        );

        boolean canWithdraw = (hostPartyCount == 0 && participantPartyCount == 0);
        long totalActiveParties = hostPartyCount + participantPartyCount;

        if(!canWithdraw) throw new ActivePartyExistsException("진행 중인 파티가 있습니다. 모든 파티를 종료하거나 나간 후 탈퇴해 주세요");

        return WithDrawValidationResponse.builder()
                .canWithdraw(canWithdraw)
                .activePartyCount(totalActiveParties)
                .hostPartyCount(hostPartyCount)
                .participantPartyCount(participantPartyCount)
                .message("탈퇴 가능합니다.")
                .build();
    }

    public void validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname))
            throw BusinessException.of(AuthErrorCode.DUPLICATED_NICKNAME);
    }

    /**
     * 재가입 가능 여부 확인
     */
    public RejoinValidationResponse validateRejoin(String email) {
        Optional<WithDrawUser> withdrawnUserOpt = withDrawUserRepository
                .findActiveWithdrawUser(email, LocalDateTime.now());

        if (withdrawnUserOpt.isEmpty()) {
            return RejoinValidationResponse.builder()
                    .canRejoin(true)
                    .message("가입 가능합니다.")
                    .build();
        }

        WithDrawUser withdrawnUser = withdrawnUserOpt.get();
        long daysRemaining = withdrawnUser.getDaysUntilRejoin();

        return RejoinValidationResponse.builder()
                .canRejoin(false)
                .daysRemaining(daysRemaining)
                .canRejoinAt(withdrawnUser.getCanRejoinAt())
                .message(String.format(
                        "탈퇴 후 30일간 재가입이 제한됩니다. %d일 후 가입 가능합니다.",
                        daysRemaining
                ))
                .build();
    }

    public List<PartyCardResponse> getHostingParties(Long userId) {
        List<Party> parties = partyRepository.findByHostUserIdAndStatus(
                userId,
                PartyStatus.RECRUITING
        );

        return parties.stream()
                .sorted(Comparator.comparing(Party::getCreatedAt).reversed())
                .map(party -> {
                    int currentParticipants = participantRepository
                            .countByPartyIdAndStatus(party.getId(), ParticipantStatus.APPROVED);
                    return PartyCardResponse.from(party, currentParticipants);
                })
                .collect(Collectors.toList());
    }

    public List<PartyCardResponse> getParticipatingParties(Long userId) {
        List<PartyParticipant> participants = participantRepository
                .findActivePartiesByUserIdExcludingHost(
                        userId,
                        PartyStatus.RECRUITING,
                        ParticipantStatus.APPROVED
                );

        return participants.stream()
                .sorted(Comparator.comparing(pp -> pp.getParty().getCreatedAt(),
                        Comparator.reverseOrder()))
                .map(pp -> {
                    Party party = pp.getParty();
                    int currentParticipants = participantRepository
                            .countByPartyIdAndStatus(party.getId(), ParticipantStatus.APPROVED);
                    return PartyCardResponse.from(party, currentParticipants);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateProfileImage(Long userId, String image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.updateProfileImage(image);
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.deleteProfileImage();
    }

    @Async
    @Transactional
    public void updateAsyncLocation(Long userId, String longitude, String latitude) {
        User user = userRepository.findById(userId).orElseThrow(() -> BusinessException.of(UserErrorCode.USER_NOT_EXISTS));
        String location = locationService.getLocation(longitude, latitude);
        user.updateLocation(location);
        userRepository.save(user);
    }
}