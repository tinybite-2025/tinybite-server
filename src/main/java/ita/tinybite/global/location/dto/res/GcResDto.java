package ita.tinybite.global.location.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GcResDto {

    public String getLocation() {
        GcRegion region = results.get(1).region;

        String[] tokens = region.area2.name.split(" ");
        String large = tokens.length == 1 ? region.area2.name : tokens[tokens.length - 1];
        String small = region.area3 != null ? region.area3.name : "";
        return large + " " + small;
    }

    private List<GcResult> results;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GcResult {
        private GcRegion region;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GcRegion {
        private GcArea area0;
        private GcArea area1;
        private GcArea area2;
        private GcArea area3;
        private GcArea area4;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GcArea {
        private String name;
    }
}
