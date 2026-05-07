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

    public String detectRegionByIp(String ip) {
        if (ip == null || ip.isBlank() || isLocalIp(ip)) {
            return "Запорізька область";
        }

        try {
            String encodedIp = URLEncoder.encode(ip, StandardCharsets.UTF_8);
            URL url = new URL("http://ip-api.com/json/" + encodedIp + "?fields=status,countryCode,regionName,city,message");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String json = response.toString();

            if (!json.contains("\"status\":\"success\"")) {
                return "Запорізька область";
            }

            if (!json.contains("\"countryCode\":\"UA\"")) {
                return "Запорізька область";
            }

            String regionName = extractValue(json, "regionName");
            String city = extractValue(json, "city");

            return mapToUkrainianRegion(regionName, city);
        } catch (Exception e) {
            return "Запорізька область";
        }
    }

    private String extractValue(String json, String key) {
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
        String value = (regionName + " " + city).toLowerCase();

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

        return "Запорізька область";
    }
}