package ita.tinybite.domain.chat.service;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 어떤 유저가 어떤 채팅방을 구독하고 있는지 저장하는 인메모리 저장소 <br>
 *<br>
 * 주의: 유저가 멀티 탭/멀티 디바이스로 같은 방을 구독할 수 있으므로<br>
 * (roomId -> userId Set)만으로는 unsubscribe/disconnect 처리 시 정확성이 떨어질 수 있다.<br>
 *<br>
 * {}sessionId, subscriptionId} 단위로 구독 추적
 * {roomId, userId} count를 유지해 현재 구독중 유저를 계산
 */
@Component
public class ChatSubscribeRegistry {

    private record SubscriptionInfo(Long roomId, Long userId) {}

    // sessionId -> (subscriptionId -> SubscriptionInfo) 하나의 세션에 여러 subscription이 생성 가능
    private final Map<String, Map<String, SubscriptionInfo>> sessionSubscriptions = new ConcurrentHashMap<>();

    // roomId -> (userId -> subscriptionCount) 같은 유저가 다른 디바이스에서 구독 가능
    private final Map<Long, Map<Long, AtomicInteger>> roomUserSubscriptionCounts = new ConcurrentHashMap<>();

    /**
     * 사용자가 세션에 접속하여 새로운 구독을 생성할 때 호출되는 메서드
     * 1. 세션Id, 구독Id, 채팅방Id, 유저Id를 종합하여 registry에 저장
     */
    public void register(String sessionId, String subscriptionId, Long roomId, Long userId) {
        if (sessionId == null || subscriptionId == null || roomId == null || userId == null) {
            return;
        }

        Map<String, SubscriptionInfo> subscriptions = sessionSubscriptions
                .computeIfAbsent(sessionId, ignored -> new ConcurrentHashMap<>());

        SubscriptionInfo newInfo = new SubscriptionInfo(roomId, userId);
        SubscriptionInfo previous = subscriptions.put(subscriptionId, newInfo);
        if (previous != null) {
            // 동일 구독을 재구독 하는 로직 방어
            if (previous.equals(newInfo)) {
                return;
            }

            decrement(previous.roomId(), previous.userId());
        }

        increment(roomId, userId);
    }

    /**
     * 특정 세션에 특정 구독을 삭제하는 메서드
     */
    public void unregister(String sessionId, String subscriptionId) {
        if (sessionId == null || subscriptionId == null) {
            return;
        }

        Map<String, SubscriptionInfo> subscriptions = sessionSubscriptions.get(sessionId);
        if (subscriptions == null) {
            return;
        }

        SubscriptionInfo removed = subscriptions.remove(subscriptionId);
        if (removed != null) {
            decrement(removed.roomId(), removed.userId());
        }

        if (subscriptions.isEmpty()) {
            sessionSubscriptions.remove(sessionId);
        }
    }

    /**
     * 특정 세션 구독을 취소할 때 호출하는 메서드 <br>
     * 세션의 모든 구독 정보를 삭제 진행
     */
    public void unregisterSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Map<String, SubscriptionInfo> removed = sessionSubscriptions.remove(sessionId);
        if (removed == null) {
            return;
        }

        removed.values().forEach(info -> decrement(info.roomId(), info.userId()));
    }

    /**
     * disconnect 이벤트 처리 메서드 <br>
     * 유저가 접속한 모든 세션 정보를 삭제 <br>
     * 1. 먼저 유저가 접속한 구독 개수 삭제
     * 2. 이후 세션 정보에 기록된 모든 userId값 삭제
     */
    public void removeUserEverywhere(Long userId) {
        if (userId == null) {
            return;
        }

        // 세션 개수
        roomUserSubscriptionCounts.values().forEach(map -> map.remove(userId));

        // 세션 정보를 순회하며, 특정 구독 정보에 유저Id가 있을 때 삭제
        sessionSubscriptions.values().forEach(map ->
                map.entrySet().removeIf(entry -> userId.equals(entry.getValue().userId()))
        );
    }

    /**
     * 특정 채팅방의 특정 유저가 구독중인지 확인하는 메서드
     */
    public boolean isSubscribed(Long roomId, Long userId) {
        Map<Long, AtomicInteger> counts = roomUserSubscriptionCounts.get(roomId);
        if (counts == null) {
            return false;
        }

        AtomicInteger count = counts.get(userId);
        return count != null && count.get() > 0;
    }

    /**
     * 채팅방을 구독중인 사용자 정보 Set으로 조회
     */
    public Set<Long> getSubscribers(Long roomId) {
        Map<Long, AtomicInteger> counts = roomUserSubscriptionCounts.get(roomId);
        if (counts == null) {
            return Collections.emptySet();
        }

        return counts.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().get() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void increment(Long roomId, Long userId) {
        roomUserSubscriptionCounts
                .computeIfAbsent(roomId, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, ignored -> new AtomicInteger(0))
                .incrementAndGet();
    }

    /**
     * 특정 채팅방(roomId)에서 특정 유저(userId)의 현재 활성 구독 개수를 1 감소
     */
    private void decrement(Long roomId, Long userId) {
        Map<Long, AtomicInteger> counts = roomUserSubscriptionCounts.get(roomId);
        if (counts == null) {
            return;
        }

        AtomicInteger count = counts.get(userId);
        if (count == null) {
            return;
        }

        // AtomicInteger 에서 개수 감소
        int next = count.decrementAndGet();

        // count가 0 이하가 되면 해당 유저는 더 이상 이 방을 구독하지 않는 것으로 간주
        if (next <= 0) {
            counts.remove(userId, count);
        }

        // 해당 방에 더 이상 구독 중인 유저가 없으면 room 자체도 정리
        if (counts.isEmpty()) {
            roomUserSubscriptionCounts.remove(roomId, counts);
        }
    }
}
