package ita.tinybite.domain.task.repository;

import ita.tinybite.domain.task.entity.Task;
import ita.tinybite.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Slice<Task> findByEventId(Long eventId, Pageable pageable);

    @Query("SELECT t " +
            "FROM Task t ")
    Slice<Task> findTodayTasksByUser(User user, Pageable pageable);

    @Query("SELECT t " +
            "FROM Task t ")
    Slice<Task> findSomedayTasksByUser(User user, Pageable pageable);
}
