package ita.tinybite.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "terms")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

    @Id
    @Column(name = "term_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String title;

    private String description;

    private boolean required;

    private int version;

    // term에는 user_term_agreement 테이블 연관관계 필요 X
}
