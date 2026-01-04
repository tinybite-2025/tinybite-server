package ita.tinybite.domain.chat.dto.res;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatMessageSliceResDto(
        List<ChatMessageResDto> messages,
        Boolean hasNext
) {

}
