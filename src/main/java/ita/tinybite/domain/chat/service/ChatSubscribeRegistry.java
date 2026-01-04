package ita.tinybite.domain.chat.service;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 어떤 유저가 어떤 채팅방을 구독하고 있는지 저장하는 인메모리 저장소
 * redis 쓰지 않은 이유는 충분히 spring 애플리케이션 내에서 구현 가능하기 때문
 */
@Component
public class ChatSubscribeRegistry {

    // roomId -> userIds
    private final Map<Long, Set<Long>> roomSubscribers = new ConcurrentHashMap<>();

    /**
     * 유저가 채널에 subscribe한 경우 추가
     */
    public void add(Long roomId, Long userId) {
        roomSubscribers
                .computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);
    }

    /**
     * 유저가 특정 채널을 unsubscribe한 경우 호출
     */
    public void remove(Long roomId, Long userId) {
        Set<Long> users = roomSubscribers.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                roomSubscribers.remove(roomId);
            }
        }
    }

    public boolean isSubscribed(Long roomId, Long userId) {
        return roomSubscribers
                .getOrDefault(roomId, Set.of())
                .contains(userId);
    }

    public void removeUserEverywhere(Long userId) {
        roomSubscribers.values().forEach(set -> set.remove(userId));
    }

    public Set<Long> getSubscribers(Long roomId) {
        return roomSubscribers.getOrDefault(roomId, Collections.emptySet());
    }
}
