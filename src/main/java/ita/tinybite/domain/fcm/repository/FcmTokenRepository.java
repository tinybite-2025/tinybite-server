package ita.tinybite.domain.fcm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ita.tinybite.domain.fcm.entity.FcmToken;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findByUserIdAndToken(Long userId, String token);
	List<FcmToken> findAllByUserIdAndIsActiveTrue(Long userId);
}
