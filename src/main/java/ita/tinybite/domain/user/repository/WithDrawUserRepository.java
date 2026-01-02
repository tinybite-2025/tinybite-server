package ita.tinybite.domain.user.repository;

import io.lettuce.core.dynamic.annotation.Param;
import ita.tinybite.domain.user.entity.WithDrawUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WithDrawUserRepository  extends JpaRepository<WithDrawUser, Long> {
    Optional<WithDrawUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT w FROM WithDrawUser w " +
            "WHERE w.email = :email " +
            "AND w.canRejoinAt > :now")
    Optional<WithDrawUser> findActiveWithdrawUser(
            @Param("email") String email,
            @Param("now") LocalDateTime now
    );
}
