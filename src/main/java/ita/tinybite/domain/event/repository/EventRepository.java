package ita.tinybite.domain.event.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ita.tinybite.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

	@Query("""
    SELECT e FROM Event e
    WHERE e.startDate <= :endDate AND e.endDate >= :startDate
    """)
	Page<Event> findAllByMonth(@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable);

	@Query("""
        SELECT e FROM Event e
        WHERE :targetDate BETWEEN e.startDate AND e.endDate
        """)
	Page<Event> findAllByDate(@Param("targetDate") LocalDate targetDate, Pageable pageable);
}
