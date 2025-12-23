package ita.tinybite.global.location;

import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.AuthErrorCode;
import ita.tinybite.global.exception.errorcode.CommonErrorCode;
import ita.tinybite.global.location.dto.res.GcResDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class LocationService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL = "https://maps.apigw.ntruss.com/map-reversegeocode/v2";
    private static final String URI = "/gc";

    public String getLocation(String latitude, String longitude) {
        String url = UriComponentsBuilder
                .fromUriString(URL + URI)
                .queryParam("coords", longitude + "," + latitude)
                .queryParam("output", "json")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-ncp-apigw-api-key-id", clientId);
        headers.add("x-ncp-apigw-api-key", secret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        GcResDto res;
        try {
             res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GcResDto.class
            ).getBody();
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }

        if(res == null) throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        if(res.getResults().isEmpty()) throw BusinessException.of(AuthErrorCode.INVALID_LOCATION);
        return res.getLocation();
    }
}
