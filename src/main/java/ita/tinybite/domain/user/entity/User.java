package ita.tinybite.domain.user.entity;

import ita.tinybite.domain.auth.dto.request.GoogleAndAppleSignupRequest;
import ita.tinybite.domain.chat.entity.ChatRoomMember;
import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("uid")
    private Long userId;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(length = 30, unique = true)
    private String nickname;

    @Column
    private String profileImage;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(length = 100)
    private String location;

    private LocalDateTime withdrawAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTermAgreement>  agreements = new ArrayList<>();;

    @OneToMany(mappedBy = "user")
    private List<ChatRoomMember> chatRoomUsers = new ArrayList<>();

    public void update(UpdateUserReqDto req) {
        this.nickname = req.nickname();
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updateSignupInfo(GoogleAndAppleSignupRequest req, String email, LoginType loginType) {
        this.email = (email);
        this.nickname = (req.nickname());
        this.phone = (req.phone());
        this.status = UserStatus.ACTIVE;
        this.type = loginType;
    }

    public void addTerms(List<UserTermAgreement> agreements) {
        this.agreements.addAll(agreements);
    }

    public void withdraw() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        this.nickname = "탈퇴한 사용자"+ uniqueId;
        this.profileImage = "/images/default-profile.jpg";
        this.email = "withdrawn_" + uniqueId + "@deleted.com";
        this.phone = String.format("010-%04d-%04d",
                new Random().nextInt(10000),
                new Random().nextInt(10000));
        this.status = UserStatus.WITHDRAW;
        this.withdrawAt = LocalDateTime.now();
    }

    // 탈퇴 여부 확인
    public boolean isWithdrawn() {
        return this.status == UserStatus.WITHDRAW;
    }

    public void updateProfileImage(String image) {
        this.profileImage = image;
    }

    public void deleteProfileImage(String defaultImage) {
        this.profileImage = defaultImage;
    }
}
