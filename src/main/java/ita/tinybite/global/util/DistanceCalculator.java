package ita.tinybite.global.util;

public class DistanceCalculator {
    // 지구의 평균 반지름 (단위: km)
    private static final int EARTH_RADIUS_KM = 6371;

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 입력값 검증
        validateCoordinates(lat1, lon1);
        validateCoordinates(lat2, lon2);

        // 같은 위치인 경우 0 반환
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        // 1. 위도와 경도의 차이를 라디안으로 변환
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // 2. Haversine 공식 적용
        // a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        // 3. 중심각 계산
        // c = 2 * atan2(√a, √(1−a))
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 4. 거리 계산 (반지름 * 중심각)
        double distance = EARTH_RADIUS_KM * c;

        return distance;
    }

    public static double calculateDistanceInMeters(double lat1, double lon1,
                                                   double lat2, double lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2) * 1000;
    }

    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // 1km 미만: 미터 단위로 표시 (반올림)
            return Math.round(distanceKm * 1000) + "m";
        } else {
            // 1km 이상: km 단위로 표시 (소수점 1자리)
            return String.format("%.1fkm", distanceKm);
        }
    }

    public static String getFormattedDistance(double lat1, double lon1,
    double lat2, double lon2) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return formatDistance(distance);
    }

    private static void validateCoordinates(double lat, double lon) {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException(
                    "위도는 -90 ~ 90 범위여야 합니다: " + lat);
        }
        if (lon < -180 || lon > 180) {
            throw new IllegalArgumentException(
                    "경도는 -180 ~ 180 범위여야 합니다: " + lon);
        }
    }

    public static boolean isWithinRadius(double centerLat, double centerLon,
                                         double targetLat, double targetLon,
                                         double radiusKm) {
        double distance = calculateDistance(centerLat, centerLon, targetLat, targetLon);
        return distance <= radiusKm;
    }
}
