package ita.tinybite.domain.chat.repository;

import ita.tinybite.domain.chat.entity.ChatRoom;
import ita.tinybite.domain.chat.entity.ChatRoomMember;
import ita.tinybite.domain.chat.enums.ChatRoomType;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.user.entity.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByUser(User user);

    Optional<ChatRoomMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    Optional<ChatRoomMember> findByChatRoomAndUserAndChatRoom_Type(ChatRoom chatRoom, User user, ChatRoomType chatRoomType);

    Optional<ChatRoomMember> findByUserAndChatRoom_typeAndChatRoom_Party(User user, ChatRoomType chatRoomType, Party chatRoomParty);
}
