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


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "one_to_one_chat_room_id")
    private ChatRoom oneToOneChatRoom;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

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

//    /**
//     * 1:1 채팅방 설정
//     */
//    public void setOneToOneChatRoom(ChatRoom chatRoom) {
//        this.oneToOneChatRoom = chatRoom;
//    }
}
