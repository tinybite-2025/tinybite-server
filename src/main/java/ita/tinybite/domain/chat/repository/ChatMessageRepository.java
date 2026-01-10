package ita.tinybite.domain.chat.repository;

import ita.tinybite.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Slice<ChatMessage> findByChatRoomId(Long roomId, Pageable pageable);

    ChatMessage findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Limit limit);

    long countByChatRoomIdAndCreatedAtAfterAndSenderIdNot(Long chatRoomId, LocalDateTime createdAtAfter, Long senderId);
}
