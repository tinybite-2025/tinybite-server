package ita.tinybite.domain.user.entity;

import ita.tinybite.domain.auth.dto.request.GoogleAndAppleSignupRequest;
import ita.tinybite.domain.user.constant.LoginType;
import ita.tinybite.domain.user.constant.UserStatus;
import ita.tinybite.domain.user.dto.req.UpdateUserReqDto;
import ita.tinybite.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

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

    @Column(nullable = false, length = 30, unique = true)
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

    @Column(nullable = false, length = 100)
    private String location;

    public void update(UpdateUserReqDto req) {
        this.nickname = req.nickname();
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updateSignupInfo(GoogleAndAppleSignupRequest req, String email) {
        this.email = (email);
        this.nickname = (req.nickname());
        this.phone = (req.phone());
        this.location = (req.location());
        this.status = UserStatus.ACTIVE;
        this.type = LoginType.GOOGLE;
    }
}
