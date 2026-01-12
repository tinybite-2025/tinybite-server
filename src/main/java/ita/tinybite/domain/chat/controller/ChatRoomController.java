package ita.tinybite.domain.chat.controller;

import ita.tinybite.domain.chat.dto.res.GroupChatRoomResDto;
import ita.tinybite.domain.chat.dto.res.OneToOneChatRoomResDto;
import ita.tinybite.domain.chat.service.ChatRoomService;
import ita.tinybite.global.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static ita.tinybite.global.response.APIResponse.success;

@RestController
@RequestMapping("/api/v1/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/one-to-one")
    public APIResponse<List<OneToOneChatRoomResDto>> getOneToOneChatRooms() {
        return success(chatRoomService.getOneToOneRooms());
    }

    @GetMapping("/group")
    public APIResponse<List<GroupChatRoomResDto>> getGroupChatRooms() {
        return success(chatRoomService.getGroupRooms());
    }
}
