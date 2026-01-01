package ita.tinybite.domain.party.entity;
import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.party.enums.ParticipantStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "one_to_one_chat_room_id")
    private ChatRoom oneToOneChatRoom;

    private ParticipantStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isApproved = false; // 승인 여부

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt; // 참여 신청 시간

    private LocalDateTime approvedAt; // 승인 시간

    private LocalDateTime rejectedAt;

    /**
     * 참여 승인
     */
    public void approve() {
        this.status = ParticipantStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 참여 거절
     */
    public void reject() {
        this.status = ParticipantStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }

    /**
     * 승인 여부
     */
    public boolean isApproved() {
        return this.status == ParticipantStatus.APPROVED;
    }
}
