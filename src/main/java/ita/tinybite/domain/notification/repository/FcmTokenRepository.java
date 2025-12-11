package ita.tinybite.domain.notification.repository;

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

	@Modifying // DML 실행 명시
	@Query("UPDATE FcmToken t SET t.isActive = :isActive WHERE t.token IN :tokens")
	int updateIsActiveByTokenIn(@Param("tokens") List<String> tokens,
		@Param("isActive") Boolean isActive);
}
