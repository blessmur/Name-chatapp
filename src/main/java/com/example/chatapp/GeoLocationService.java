package com.example.chatapp;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GeoLocationService {

    public GeoInfo detectGeoByIp(String ip) {
        if (ip == null || ip.isBlank() || isLocalIp(ip)) {
            return defaultGeo();
        }

        try {
            String encodedIp = URLEncoder.encode(ip, StandardCharsets.UTF_8);

            URL url = new URL(
                    "http://ip-api.com/json/" + encodedIp +
                            "?fields=status,countryCode,regionName,city,lat,lon,message"
            );

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String json = response.toString();

            if (!json.contains("\"status\":\"success\"")) {
                return defaultGeo();
            }

            if (!json.contains("\"countryCode\":\"UA\"")) {
                return defaultGeo();
            }

            String regionName = extractStringValue(json, "regionName");
            String city = extractStringValue(json, "city");
            Double latitude = extractDoubleValue(json, "lat");
            Double longitude = extractDoubleValue(json, "lon");

            String region = mapToUkrainianRegion(regionName, city);

            if (latitude == null || longitude == null) {
                RegionCenter center = getRegionCenter(region);
                latitude = center.latitude;
                longitude = center.longitude;
            }

            return new GeoInfo(
                    ip,
                    region,
                    city == null || city.isBlank() ? "Невідоме місто" : city,
                    latitude,
                    longitude
            );

        } catch (Exception e) {
            return defaultGeo();
        }
    }

    public String detectRegionByIp(String ip) {
        return detectGeoByIp(ip).getRegion();
    }

    private GeoInfo defaultGeo() {
        return new GeoInfo(
                "local",
                "Невідома область",
                "Невідоме місто",
                0.0,
                0.0
        );
    }

    private String extractStringValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);

        if (start == -1) {
            return "";
        }

        start += search.length();
        int end = json.indexOf("\"", start);

        if (end == -1) {
            return "";
        }

        return json.substring(start, end);
    }

    private Double extractDoubleValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length();

        int endComma = json.indexOf(",", start);
        int endBrace = json.indexOf("}", start);

        int end;

        if (endComma == -1) {
            end = endBrace;
        } else if (endBrace == -1) {
            end = endComma;
        } else {
            end = Math.min(endComma, endBrace);
        }

        if (end == -1) {
            return null;
        }

        try {
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isLocalIp(String ip) {
        return ip.equals("127.0.0.1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equals("::1")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.20.")
                || ip.startsWith("172.21.")
                || ip.startsWith("172.22.")
                || ip.startsWith("172.23.")
                || ip.startsWith("172.24.")
                || ip.startsWith("172.25.")
                || ip.startsWith("172.26.")
                || ip.startsWith("172.27.")
                || ip.startsWith("172.28.")
                || ip.startsWith("172.29.")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.");
    }

    private String mapToUkrainianRegion(String regionName, String city) {
        String value =
                ((regionName == null ? "" : regionName)
                        + " "
                        + (city == null ? "" : city)).toLowerCase();

        Map<String, String> regions = new LinkedHashMap<>();

        regions.put("vinnytsia", "Вінницька область");
        regions.put("volyn", "Волинська область");
        regions.put("dnipropetrovsk", "Дніпропетровська область");
        regions.put("dnipro", "Дніпропетровська область");
        regions.put("donetsk", "Донецька область");
        regions.put("zhytomyr", "Житомирська область");
        regions.put("zakarpattia", "Закарпатська область");
        regions.put("uzhhorod", "Закарпатська область");
        regions.put("zaporizhzhia", "Запорізька область");
        regions.put("zaporizhia", "Запорізька область");
        regions.put("ivano-frankivsk", "Івано-Франківська область");
        regions.put("kyiv", "Київська область");
        regions.put("kiev", "Київська область");
        regions.put("kirovohrad", "Кіровоградська область");
        regions.put("kropyvnytskyi", "Кіровоградська область");
        regions.put("luhansk", "Луганська область");
        regions.put("lugansk", "Луганська область");
        regions.put("lviv", "Львівська область");
        regions.put("mykolaiv", "Миколаївська область");
        regions.put("nikolaev", "Миколаївська область");
        regions.put("odesa", "Одеська область");
        regions.put("odessa", "Одеська область");
        regions.put("poltava", "Полтавська область");
        regions.put("rivne", "Рівненська область");
        regions.put("sumy", "Сумська область");
        regions.put("ternopil", "Тернопільська область");
        regions.put("kharkiv", "Харківська область");
        regions.put("kherson", "Херсонська область");
        regions.put("khmelnytskyi", "Хмельницька область");
        regions.put("cherkasy", "Черкаська область");
        regions.put("chernivtsi", "Чернівецька область");
        regions.put("chernihiv", "Чернігівська область");
        regions.put("chernigov", "Чернігівська область");

        for (Map.Entry<String, String> entry : regions.entrySet()) {
            if (value.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "Невідома область";
    }

    private RegionCenter getRegionCenter(String region) {
        Map<String, RegionCenter> centers = new LinkedHashMap<>();

        centers.put("Вінницька область", new RegionCenter(49.2328, 28.4810));
        centers.put("Волинська область", new RegionCenter(50.7472, 25.3254));
        centers.put("Дніпропетровська область", new RegionCenter(48.4647, 35.0462));
        centers.put("Донецька область", new RegionCenter(48.0159, 37.8029));
        centers.put("Житомирська область", new RegionCenter(50.2547, 28.6587));
        centers.put("Закарпатська область", new RegionCenter(48.6208, 22.2879));
        centers.put("Запорізька область", new RegionCenter(47.8388, 35.1396));
        centers.put("Івано-Франківська область", new RegionCenter(48.9226, 24.7111));
        centers.put("Київська область", new RegionCenter(50.4501, 30.5234));
        centers.put("Кіровоградська область", new RegionCenter(48.5079, 32.2623));
        centers.put("Луганська область", new RegionCenter(48.5740, 39.3078));
        centers.put("Львівська область", new RegionCenter(49.8397, 24.0297));
        centers.put("Миколаївська область", new RegionCenter(46.9750, 31.9946));
        centers.put("Одеська область", new RegionCenter(46.4825, 30.7233));
        centers.put("Полтавська область", new RegionCenter(49.5883, 34.5514));
        centers.put("Рівненська область", new RegionCenter(50.6199, 26.2516));
        centers.put("Сумська область", new RegionCenter(50.9077, 34.7981));
        centers.put("Тернопільська область", new RegionCenter(49.5535, 25.5948));
        centers.put("Харківська область", new RegionCenter(49.9935, 36.2304));
        centers.put("Херсонська область", new RegionCenter(46.6354, 32.6169));
        centers.put("Хмельницька область", new RegionCenter(49.4229, 26.9871));
        centers.put("Черкаська область", new RegionCenter(49.4444, 32.0598));
        centers.put("Чернівецька область", new RegionCenter(48.2915, 25.9403));
        centers.put("Чернігівська область", new RegionCenter(51.4982, 31.2893));

        return centers.getOrDefault(region, new RegionCenter(0.0, 0.0));
    }

    private static class RegionCenter {
        private final Double latitude;
        private final Double longitude;

        private RegionCenter(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class GeoInfo {
        private String ip;
        private String region;
        private String city;
        private Double latitude;
        private Double longitude;

        public GeoInfo(String ip, String region, String city, Double latitude, Double longitude) {
            this.ip = ip;
            this.region = region;
            this.city = city;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getIp() {
            return ip;
        }

        public String getRegion() {
            return region;
        }

        public String getCity() {
            return city;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    }
}