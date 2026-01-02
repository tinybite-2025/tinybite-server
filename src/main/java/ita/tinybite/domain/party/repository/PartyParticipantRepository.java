package ita.tinybite.domain.party.repository;

import io.lettuce.core.dynamic.annotation.Param;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyParticipantRepository extends JpaRepository<PartyParticipant, Long> {

    boolean existsByPartyAndUser(Party party, User user);

    boolean existsByPartyAndUserUserIdAndStatus(Party party, Long userId, ParticipantStatus status);

    List<PartyParticipant> findByPartyAndStatus(Party party, ParticipantStatus status);

    boolean existsByPartyAndUserAndStatus(Party party, User user, ParticipantStatus status);

    @Query("SELECT pp FROM PartyParticipant pp " +
            "WHERE pp.user.id = :userId " +
            "AND pp.party.status =:partyStatus " +
            "AND pp.status = :participantStatus")
    List<PartyParticipant> findActivePartiesByUserId(
            @Param("userId") Long userId,
            @Param("partyStatuses") PartyStatus partyStatus,
            @Param("participantStatus") ParticipantStatus participantStatus
    );

    @Query("SELECT COUNT(pp) FROM PartyParticipant pp " +
            "WHERE pp.party.id = :partyId " +
            "AND pp.status = :status")
    int countByPartyIdAndStatus(
            @Param("partyId") Long partyId,
            @Param("status") ParticipantStatus status
    );

}