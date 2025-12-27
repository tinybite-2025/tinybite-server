package ita.tinybite.domain.party.enums;

import lombok.Getter;

@Getter
public enum PartyCategory {
    ALL("전체"),
    DELIVERY("배달"),
    GROCERY("장보기"),
    HOUSEHOLD("생활용품");

    private final String description;

    PartyCategory(String description) {
        this.description = description;
    }
}
