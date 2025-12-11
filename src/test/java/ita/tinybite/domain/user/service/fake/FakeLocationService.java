package ita.tinybite.domain.user.service.fake;

import ita.tinybite.global.location.LocationService;

public class FakeLocationService extends LocationService {

    @Override
    public String getLocation(String latitude, String longitude) {
        return latitude + " " + longitude;
    }
}
