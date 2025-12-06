package ita.tinybite.global.location;

import ita.tinybite.global.response.APIResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static ita.tinybite.global.response.APIResponse.*;

@RestController
@RequestMapping("/api/v1/auth/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public APIResponse<String> location(@RequestParam(defaultValue = "37.3623504988728") String latitude,
                                        @RequestParam(defaultValue = "127.117057453619") String longitude) {
        return success(locationService.getLocation(latitude, longitude));
    }
}
