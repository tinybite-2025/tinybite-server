package ita.tinybite.domain.user.repository;

import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // status == ACTIVE인 user의 nickname에서 찾음
    boolean existsByStatusAndNickname(UserStatus status, String nickname);

    boolean existsByNickname(String nickname);

    User getUserByUserId(Long userId);
}
