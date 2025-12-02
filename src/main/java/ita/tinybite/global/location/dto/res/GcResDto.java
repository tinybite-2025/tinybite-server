package ita.tinybite.global.location.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GcResDto {


    public String getLocation() {
        List<String> address = new ArrayList<>();

        GcRegion region = results.get(0).region;

        if (region.area0 != null) address.add(region.area0.name);
        if (region.area1 != null) address.add(region.area1.name);
        if (region.area2 != null) address.add(region.area2.name);
        if (region.area3 != null) address.add(region.area3.name);
        if (region.area4 != null) address.add(region.area4.name);

        return address.get(address.size() - 3) + " "  + address.get(address.size() - 2);
    }

    private GcStatus status;
    private List<GcResult> results;

    @Getter
    static class GcStatus {
        private int code;
        private String name;
        private String message;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GcResult {
        private String name;
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
