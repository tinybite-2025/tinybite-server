package ita.tinybite.domain.party.repository;

import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.PartyCategory;
import java.util.List;
import java.util.Optional;

import ita.tinybite.domain.party.enums.PartyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

    @Query("SELECT p FROM Party p JOIN FETCH p.host WHERE p.id = :id")
    Optional<Party> findByIdWithHost(@Param("id") Long id);

    List<Party> findByPickupLocation_Place(String place);

    List<Party> findByPickupLocation_PlaceAndCategory(String place, PartyCategory category);

    List<Party> findByHostUserIdAndStatus(Long userId, PartyStatus partyStatus);

    List<Party> findByHostUserIdAndStatusIn(Long userId, List<PartyStatus> statuses);

    List<Party> findByTown(String location);

    List<Party> findByTownAndCategory(String location, PartyCategory category);
}
