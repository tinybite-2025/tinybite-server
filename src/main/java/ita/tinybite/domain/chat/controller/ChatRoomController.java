package ita.tinybite.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import ita.tinybite.domain.chat.dto.GroupChatRoomDetailResDto;
import ita.tinybite.domain.chat.dto.res.GroupChatRoomResDto;
import ita.tinybite.domain.chat.dto.res.OneToOneChatRoomDetailResDto;
import ita.tinybite.domain.chat.dto.res.OneToOneChatRoomResDto;
import ita.tinybite.domain.chat.service.ChatRoomService;
import ita.tinybite.global.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "일대일 채팅방 접속 시 필요한 정보들을 조회합니다.", description = """
            승인 대기, 승인 요청, 승인 수락, 승인 거절 등 <br>
            호스트와 참여자 관계에 따른 상태 변화가 반영됩니다.
            """)
    @GetMapping("/one-to-one/{chatRoomId}")
    public APIResponse<OneToOneChatRoomDetailResDto> getOneToOneChatRoomInfo(
            @PathVariable Long chatRoomId) {
        return success(chatRoomService.getOneToOneRoom(chatRoomId));
    }

    @Operation(summary = "그룹 채팅방 접속 시 필요한 정보들을 조회합니다.", description = "현재 개발중입니다.")
    @GetMapping("/group/{chatRoomId}")
    public APIResponse<GroupChatRoomDetailResDto> getGroupChatRoomInfo(
            @PathVariable Long chatRoomId) {
        return success(chatRoomService.getGroupRoom(chatRoomId));
    }
}
