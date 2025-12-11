package ita.tinybite.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ita.tinybite.domain.notification.entity.FcmToken;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findByUserIdAndToken(Long userId, String token);
	List<FcmToken> findAllByUserIdAndIsActiveTrue(Long userId);
	List<FcmToken> findAllByUserIdInAndIsActiveTrue(List<Long> userIds);

	@Modifying
	@Query("UPDATE FcmToken t SET t.isActive = :isActive WHERE t.token IN :tokens")
	int updateIsActiveByTokenIn(@Param("tokens") List<String> tokens,
		@Param("isActive") Boolean isActive);

	@Modifying
	@Query("DELETE FROM FcmToken t WHERE t.isActive = FALSE AND t.updatedAt < :cutoffTime")
	int deleteByIsActiveFalseAndUpdatedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
}
