package ita.tinybite.domain.party.dto.response;
import ita.tinybite.domain.party.entity.Party;
import ita.tinybite.domain.party.enums.ParticipantStatus;
import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.enums.PartyStatus;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCardResponse {
    private Long partyId;
    private String thumbnailImage; // 첫 번째 이미지 또는 기본 이미지
    private String title;
    private Integer pricePerPerson; // 1/N 가격
    private String participantStatus; // "1/4명"
    private String distance; // "300m" or "1.2km" (화면 표시용)
    private Double distanceKm; // km 단위 거리 (정렬용)
    private String timeAgo; // "10분 전", "3시간 전"
    private Boolean isClosed; // 마감 여부
    private PartyCategory category;
    private LocalDateTime createdAt;

    public static PartyCardResponse from(Party party, int currentParticipants, boolean isHost, ParticipantStatus status) {
        return PartyCardResponse.builder()
                .partyId(party.getId())
                .thumbnailImage(party.getThumbnailImage())
                .title(party.getTitle())
                .pricePerPerson(calculatePricePerPerson(party, currentParticipants))
                .participantStatus(formatParticipantStatus(currentParticipants, party.getMaxParticipants()))
                .distance(null) // 거리 계산은 별도 처리 필요
                .distanceKm(null) // 거리 계산은 별도 처리 필요
                .timeAgo(calculateTimeAgo(party.getCreatedAt()))
                .isClosed(checkIfClosed(party, currentParticipants))
                .category(party.getCategory())
                .createdAt(party.getCreatedAt())
                .build();
    }
    private static String getThumbnailImage(Party party) {
        if (party.getImage() != null && !party.getImage().isEmpty()) {
            return party.getImage();
        }
        return "/images/default-party-thumbnail.jpg"; // 기본 이미지
    }

    /**
     * 1/N 가격 계산
     */
    private static Integer calculatePricePerPerson(Party party, int currentParticipants) {
        if (party.getPrice() == null || currentParticipants == 0) {
            return null;
        }
        return party.getPrice() / currentParticipants;
    }

    /**
     * 참가자 상태 포맷팅 "1/4명"
     */
    private static String formatParticipantStatus(int current, int max) {
        return String.format("%d/%d명", current, max);
    }

    /**
     * 시간 경과 계산 "10분 전", "3시간 전"
     */
    private static String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 7) {
            return days + "일 전";
        } else {
            return createdAt.format(DateTimeFormatter.ofPattern("MM.dd"));
        }
    }

    /**
     * 마감 여부 확인
     */
    private static Boolean checkIfClosed(Party party, int currentParticipants) {
        // 1. 파티 상태가 CLOSED인 경우
        if (party.getStatus() == PartyStatus.CLOSED) {
            return true;
        }

        // 2. 정원이 다 찬 경우
        if (currentParticipants >= party.getMaxParticipants()) {
            return true;
        }

        return false;
    }
}