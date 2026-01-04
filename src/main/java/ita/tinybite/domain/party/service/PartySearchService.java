package ita.tinybite.domain.party.service;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.party.dto.request.PartyQueryListResponse;
import ita.tinybite.domain.party.dto.response.PartyCardResponse;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.repository.PartyParticipantRepository;
import ita.tinybite.domain.party.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartySearchService {

    private final PartyRepository partyRepository;
    private final PartyParticipantRepository participantRepository;
    private final StringRedisTemplate redisTemplate;
    private final SecurityProvider securityProvider;


    private static final String KEY_PREFIX = "recent_search:";
    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    // 파티 검색 조회
    public PartyQueryListResponse searchParty(String q, PartyCategory category, int page, int size) {
        Long userId = securityProvider.getCurrentUser().getUserId();

        // recent_search:{userId}
        String key = key(userId);

        redisTemplate.opsForZSet().remove(key, q);
        redisTemplate.opsForZSet().add(key, q, System.currentTimeMillis());

        Pageable pageable = PageRequest.of(page, size);

        // category가 없을 시에는 ALL로 처리
        Page<Party> result = (category == null || category == PartyCategory.ALL)
                ? partyRepository.findByTitleContaining(q, pageable)
                : partyRepository.findByTitleContainingAndCategory(q, category, pageable);

        List<PartyCardResponse> partyCardResponseList = result.stream()
                .map(party -> {
                    int currentParticipants = participantRepository
                            .countByPartyIdAndStatus(party.getId(), ParticipantStatus.APPROVED);
                    return PartyCardResponse.from(party, currentParticipants);
                })
                .toList();

        return PartyQueryListResponse.builder()
                .parties(partyCardResponseList)
                .hasNext(result.hasNext())
                .build();
    }


    // 최근 검색어 20개 조회
    public List<String> getLog() {
        Long userId = securityProvider.getCurrentUser().getUserId();
         return redisTemplate.opsForZSet()
                .reverseRange(key(userId), 0, 19)
                 .stream().toList();
    }

    public void deleteLog(String keyword) {
        Long userId = securityProvider.getCurrentUser().getUserId();
        redisTemplate.opsForZSet().remove(key(userId), keyword);
    }

    public void deleteAllLog() {
        Long userId = securityProvider.getCurrentUser().getUserId();
        redisTemplate.delete(key(userId));
    }
}
