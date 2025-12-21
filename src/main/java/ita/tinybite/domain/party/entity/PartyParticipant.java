package ita.tinybite.domain.party.entity;
import ita.tinybite.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "party_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartyParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isApproved = false; // 승인 여부

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt; // 참여 신청 시간

    private LocalDateTime approvedAt; // 승인 시간

    /**
     * 참여 승인
     */
    public void approve() {
        this.isApproved = true;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 승인 취소
     */
    public void reject() {
        this.isApproved = false;
        this.approvedAt = null;
    }
}
