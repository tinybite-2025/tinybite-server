package ita.tinybite.domain.user.service;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.domain.user.dto.res.PartyResponse;
import ita.tinybite.domain.user.dto.res.UserResDto;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.AuthErrorCode;
import ita.tinybite.global.location.LocationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SecurityProvider securityProvider;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final PartyParticipantRepository participantRepository;

    public UserService(SecurityProvider securityProvider,
                       UserRepository userRepository,
                       LocationService locationService,
                       PartyParticipantRepository participantRepository) {
        this.securityProvider = securityProvider;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.participantRepository = participantRepository;
    }

    public UserResDto getUser() {
        User user = securityProvider.getCurrentUser();
        return UserResDto.of(user);
    }

    public void updateUser(UpdateUserReqDto req) {
        User user = securityProvider.getCurrentUser();
        user.update(req);
    }

    public void updateLocation(String latitude, String longitude) {
        User user = securityProvider.getCurrentUser();
        String location = locationService.getLocation(latitude, longitude);
        user.updateLocation(location);
    }

    public void deleteUser() {
        userRepository.delete(securityProvider.getCurrentUser());
    }

    public void validateNickname(String nickname) {
        if(userRepository.existsByNickname(nickname))
            throw BusinessException.of(AuthErrorCode.DUPLICATED_NICKNAME);
    }

    public List<PartyResponse> getActiveParties(Long userId) {
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
                    boolean isHost = party.getHost().getUserId().equals(userId);
                    return PartyResponse.from(party, currentParticipants, isHost,pp.getStatus());
                })
                .collect(Collectors.toList());
    }
}
