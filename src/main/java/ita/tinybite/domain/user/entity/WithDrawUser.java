package ita.tinybite.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "withdrawn_users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithDrawUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;  // 또는 소셜 로그인 ID

    @Column(nullable = false)
    private LocalDateTime withdrawnAt;

    @Column(nullable = false)
    private LocalDateTime canRejoinAt;  // 재가입 가능 일시 (탈퇴 + 30일)

    public static WithDrawUser from(User user) {
        LocalDateTime withdrawnAt = LocalDateTime.now();
        return WithDrawUser.builder()
                .email(user.getEmail())
                .withdrawnAt(withdrawnAt)
                .canRejoinAt(withdrawnAt.plusDays(30))
                .build();
    }

    public boolean canRejoin() {
        return LocalDateTime.now().isAfter(canRejoinAt);
    }

    public long getDaysUntilRejoin() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), canRejoinAt);
    }
}
