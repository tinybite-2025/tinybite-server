package ita.tinybite.domain.party.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PickupLocation {
    String place;
    String lat;
    String lon;
}
