package ita.tinybite.domain.party.entity;


import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.party.enums.PartyStatus;
import ita.tinybite.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "party")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title; // 파티 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 설명

    @Column(length = 500)
    private String thumbnailImage; // 섬네일 이미지 URL

    @Column(length = 500)
    private String image; // 이미지 URL

    @Column(nullable = false)
    private Integer price; // 가격

    @Column(nullable = false)
    private Integer maxParticipants; // 최대 인원

    @Column(nullable = false)
    private Integer currentParticipants;

    @Column(length = 500)
    private String link; // 링크 (예: 배달앱 링크)

    @Embedded
    @Column
    private PickupLocation pickupLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartyCategory category; // 카테고리

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyStatus status;

    @Column(nullable = false)
    private Boolean isClosed; // 마감 여부

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 등록시간

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host; // 파티 개설자

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PartyParticipant> participants = new ArrayList<>(); // 파티 참여 유저

    /**
     * 참여자 수 증가
     */
    public void incrementParticipants() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("파티 인원이 가득 찼습니다");
        }
        this.currentParticipants++;
    }

    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();

        // 전체 경과 시간을 분 단위로 계산
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();

        // 1분 미만
        if (minutes < 1) {
            return "방금 전";
        }

        // 1시간 미만 (1~59분)
        if (minutes < 60) {
            return minutes + "분 전";
        }

        // 24시간 미만 (1~23시간)
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }

        // 30일 미만 (1~29일)
        long days = hours / 24;
        if (days < 30) {
            return days + "일 전";
        }

        // 12개월 미만 (1~11개월)
        long months = days / 30;
        if (months < 12) {
            return months + "개월 전";
        }

        // 1년 이상
        long years = months / 12;
        return years + "년 전";
    }

    public void updateAllFields(String title, Integer price, Integer maxParticipants,
                                PickupLocation pickupLocation, Double latitude, Double longitude,
                                String productLink, String description, List<String> images) {
        this.title = title != null ? title : this.title;
        this.price = price != null ? price : this.price;
        this.maxParticipants = maxParticipants != null ? maxParticipants : this.maxParticipants;
        this.pickupLocation = pickupLocation != null ? pickupLocation : this.pickupLocation;

        // 상품 링크 검증
        if (productLink != null) {
            if (this.category == PartyCategory.DELIVERY) {
                throw new IllegalArgumentException("배달 파티는 상품 링크를 추가할 수 없습니다");
            }
            this.link = productLink;
        }

        this.description = description != null ? description : this.description;

        if (images != null && !images.isEmpty()) {
            this.image = images.get(0);
            this.thumbnailImage = images.get(0);
        }
    }

    public void updateLimitedFields(String description, List<String> images) {
        this.description = description != null ? description : this.description;

        if (images != null && !images.isEmpty()) {
            this.image = images.get(0);
            this.thumbnailImage = images.get(0);
        }
    }

    public void close() {
        validateCanClose();
        this.status = PartyStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * 파티 종료 가능 여부 검증
     */
    private void validateCanClose() {
        if (this.status == PartyStatus.CLOSED) {
            throw new IllegalStateException("이미 종료된 파티입니다.");
        }
        if (this.status == PartyStatus.CANCELLED) {
            throw new IllegalStateException("취소된 파티는 종료할 수 없습니다.");
        }
    }
}
