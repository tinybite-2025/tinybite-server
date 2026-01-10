package ita.tinybite.domain.chat.entity;

import ita.tinybite.domain.chat.enums.MessageType;
import ita.tinybite.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    // 채팅룸 아이디
    private Long chatRoomId;
    // 전송자 아이디
    private Long senderId;
    // 전송자 이름 (nickname)
    private String senderName;
    // 메시지 내용
    private String content;
}

