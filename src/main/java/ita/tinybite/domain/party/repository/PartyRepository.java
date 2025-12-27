package ita.tinybite.domain.party.repository;

import ita.tinybite.domain.party.entity.Neighborhood;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.PartyCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

    List<Party> findByPickupLocation_Place(String place);

    List<Party> findByPickupLocation_PlaceAndCategory(String place, PartyCategory category);
}

