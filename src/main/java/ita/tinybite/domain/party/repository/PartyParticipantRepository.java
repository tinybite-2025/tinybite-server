package ita.tinybite.domain.party.repository;

import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.entity.PartyParticipant;
import ita.tinybite.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyParticipantRepository extends JpaRepository<PartyParticipant, Long> {

    boolean existsByPartyAndUser(Party party, User user);

    boolean existsByPartyAndUserAndIsApprovedTrue(Party party, User user);
}