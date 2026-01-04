package ita.tinybite.domain.chat.entity;

import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoomMember> participants = new ArrayList<>();

    // ========== 비즈니스 메서드 ==========

    public void addParticipants(ChatRoomMember... participants) {
        this.participants.addAll(List.of(participants));
    }

    /**
     * 멤버 추가
     */
    public void addMember(User user) {
        ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(this)
                .user(user)
                .isActive(true)
                .build();
        participants.add(member);
    }

    /**
     * 채팅방 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}
