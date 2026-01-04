package ita.tinybite.domain.party.enums;

import lombok.Getter;

@Getter
public enum PartySortType {
    LATEST("최신순"),
    DISTANCE("거리순");

    private final String description;

    PartySortType(String description) {
        this.description = description;
    }
}