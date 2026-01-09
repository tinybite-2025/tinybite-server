package ita.tinybite.domain.party.repository;

import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.PartyCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartySearchRepository extends JpaRepository<Party, Long> {

    Page<Party> findByTitleContaining(String title, Pageable pageable);

    Page<Party> findByTitleContainingAndCategory(String title, PartyCategory category, Pageable pageable);

    @Query(value = """
      SELECT p.*
      FROM party p
      WHERE p.title LIKE CONCAT('%', :title, '%')
      ORDER BY (6371000 * acos(
          cos(radians(:lat)) * cos(radians(p.pickup_latitude))
        * cos(radians(p.pickup_longitude) - radians(:lon))
        + sin(radians(:lat)) * sin(radians(p.pickup_latitude))))
      """, countQuery = """
      SELECT COUNT(*)
      FROM party p
      WHERE p.title LIKE CONCAT('%', :title, '%')
      """, nativeQuery = true)
    Page<Party> findByTitleContainingWithDistance(String title, @Param("lat") Double lat, @Param("lon") Double lon, Pageable pageable);

    @Query(value = """
    SELECT p.*
    FROM party p
    WHERE p.title LIKE CONCAT('%', :title, '%')
    ORDER BY (6371000 * acos(
          cos(radians(:lat)) * cos(radians(p.pickup_latitude))
        * cos(radians(p.pickup_longitude) - radians(:lon))
        + sin(radians(:lat)) * sin(radians(p.pickup_latitude))))
    """, countQuery = """
    SELECT COUNT(*)
    FROM party p
    WHERE p.title LIKE CONCAT('%', :title, '%')
    """, nativeQuery = true)
    Page<Party> findByTitleContainingAndCategoryWithDistance(String title, @Param("lat") Double lat, @Param("lon") Double lon, PartyCategory category, Pageable pageable);
}
