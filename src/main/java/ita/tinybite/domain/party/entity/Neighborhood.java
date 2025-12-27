package ita.tinybite.domain.party.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "neighborhood")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Neighborhood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 예: "공릉1동", "공릉2동"
}
