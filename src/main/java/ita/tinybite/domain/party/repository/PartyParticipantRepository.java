package ita.tinybite.domain.party.repository;

import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyParticipantRepository extends JpaRepository<PartyParticipant, Long> {

    boolean existsByPartyAndUser(Party party, User user);

    boolean existsByPartyAndUserUserIdAndStatus(Party party, Long userId, ParticipantStatus status);

    List<PartyParticipant> findByPartyAndStatus(Party party, ParticipantStatus status);

    boolean existsByPartyAndUserAndStatus(Party party, User user, ParticipantStatus status);

    @Query("SELECT pp FROM PartyParticipant pp " +
            "WHERE pp.user.userId = :userId " +
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

    /**
     * 사용자가 참여중인 파티 개수 조회 (호스트 + 참가자)
     */
    @Query("SELECT COUNT(DISTINCT pp.party.id) " +
            "FROM PartyParticipant pp " +
            "WHERE pp.user.userId = :userId " +
            "AND pp.party.status IN :activeStatuses " +
            "AND pp.status = :participantStatus")
    long countActivePartiesByUserId(
            @Param("userId") Long userId,
            @Param("activeStatuses") List<PartyStatus> activeStatuses,
            @Param("participantStatus") ParticipantStatus participantStatus
    );

    /**
     * 사용자가 호스트인 활성 파티 개수
     */
    @Query("SELECT COUNT(p) FROM Party p " +
            "WHERE p.host.userId = :userId " +
            "AND p.status IN :activeStatuses")
    long countActivePartiesByHostId(
            @Param("userId") Long userId,
            @Param("activeStatuses") List<PartyStatus> activeStatuses
    );

    @Query("SELECT pp FROM PartyParticipant pp " +
            "JOIN FETCH pp.party p " +
            "JOIN FETCH p.host " +
            "WHERE pp.user.userId = :userId " +
            "AND p.host.userId != :userId " +
            "AND p.status = :partyStatus " +
            "AND pp.status = :participantStatus")
    List<PartyParticipant> findActivePartiesByUserIdExcludingHost(
            @Param("userId") Long userId,
            @Param("partyStatus") PartyStatus partyStatus,
            @Param("participantStatus") ParticipantStatus participantStatus
    );

    int countByPartyIdAndStatusAndUser_UserIdNot(Long partyId, ParticipantStatus participantStatus, Long userId);

    List<PartyParticipant> findAllByPartyAndStatus(Party party, ParticipantStatus status);

    boolean existsByParty_IdAndUser_UserIdAndStatus(
        Long partyId,
        Long userId,
        ParticipantStatus status
    );
}