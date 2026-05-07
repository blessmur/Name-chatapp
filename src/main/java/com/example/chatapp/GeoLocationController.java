package com.example.chatapp;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GeoLocationController {

    private final GeoLocationService geoLocationService;

    public GeoLocationController(GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    @GetMapping("/api/geo/region")
    public Map<String, Object> detectRegion(HttpServletRequest request) {
        String ip = getClientIp(request);
        GeoLocationService.GeoInfo geoInfo = geoLocationService.detectGeoByIp(ip);

        return Map.of(
                "ip", ip,
                "region", geoInfo.getRegion(),
                "city", geoInfo.getCity(),
                "latitude", geoInfo.getLatitude(),
                "longitude", geoInfo.getLongitude()
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }
}