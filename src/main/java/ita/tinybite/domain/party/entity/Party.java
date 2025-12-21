package ita.tinybite.domain.party.entity;


import ita.tinybite.domain.party.enums.PartyCategory;
import ita.tinybite.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(length = 500)
    private String link; // 링크 (예: 배달앱 링크)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartyCategory category; // 카테고리

    @Column(nullable = false)
    private Double latitude; // 위도 (거리 계산용)

    @Column(nullable = false)
    private Double longitude; // 경도 (거리 계산용)

    @Column(nullable = false)
    private Boolean isClosed; // 마감 여부

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 등록시간

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood; // 동네

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host; // 파티 개설자

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PartyParticipant> participants = new ArrayList<>(); // 파티 참여 유저

}
