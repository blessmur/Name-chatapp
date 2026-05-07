package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private final Map<String, OnlineUser> onlineUsers = new ConcurrentHashMap<>();

    @MessageMapping("/join")
    public void join(ChatMessage message) {
        String username = message.getUsername();
        String region = message.getRegion();
        String status = message.getStatus() != null ? message.getStatus() : "ONLINE";
        String city = message.getCity() != null ? message.getCity() : "Невідоме місто";
        Double latitude = message.getLatitude() != null ? message.getLatitude() : 47.8388;
        Double longitude = message.getLongitude() != null ? message.getLongitude() : 35.1396;

        onlineUsers.put(username, new OnlineUser(username, region, city, status, latitude, longitude));

        ChatMessage joinMessage = new ChatMessage(
                "System",
                username + " приєднався до чату з області: " + region,
                region,
                "JOIN"
        );

        joinMessage.setCity(city);
        joinMessage.setLatitude(latitude);
        joinMessage.setLongitude(longitude);

        chatMessageRepository.save(joinMessage);
        messagingTemplate.convertAndSend("/topic/messages", joinMessage);
        sendStats();
    }

    @MessageMapping("/leave")
    public void leave(ChatMessage message) {
        onlineUsers.remove(message.getUsername());

        ChatMessage leaveMessage = new ChatMessage(
                "System",
                message.getUsername() + " вийшов з чату",
                message.getRegion(),
                "LEAVE"
        );

        leaveMessage.setCity(message.getCity());
        leaveMessage.setLatitude(message.getLatitude());
        leaveMessage.setLongitude(message.getLongitude());

        chatMessageRepository.save(leaveMessage);
        messagingTemplate.convertAndSend("/topic/messages", leaveMessage);
        sendStats();
    }

    @MessageMapping("/room")
    public void sendToRoom(ChatMessage message) {
        chatMessageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/room/" + message.getType(), message);
    }

    @MessageMapping("/private")
    public void privateMessage(ChatMessage message) {
        message.setType("PRIVATE");
        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/private/" + message.getRecipient(), message);
        messagingTemplate.convertAndSend("/topic/private/" + message.getUsername(), message);
    }

    @MessageMapping("/status")
    public void changeStatus(ChatMessage message) {
        OnlineUser oldUser = onlineUsers.get(message.getUsername());

        if (oldUser != null) {
            String region = message.getRegion() != null ? message.getRegion() : oldUser.getRegion();
            String city = message.getCity() != null ? message.getCity() : oldUser.getCity();
            String status = message.getStatus() != null ? message.getStatus() : oldUser.getStatus();
            Double latitude = message.getLatitude() != null ? message.getLatitude() : oldUser.getLatitude();
            Double longitude = message.getLongitude() != null ? message.getLongitude() : oldUser.getLongitude();

            onlineUsers.put(
                    message.getUsername(),
                    new OnlineUser(message.getUsername(), region, city, status, latitude, longitude)
            );
        }

        sendStats();
    }

    @MessageMapping("/typing")
    public void typing(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/typing", message);
    }

    @GetMapping("/api/messages/general")
    @ResponseBody
    public List<ChatMessage> getGeneralMessages() {
        List<ChatMessage> result = new ArrayList<>();
        result.addAll(chatMessageRepository.findTop100ByTypeOrderByCreatedAtAsc("JOIN"));
        result.addAll(chatMessageRepository.findTop100ByTypeOrderByCreatedAtAsc("LEAVE"));
        result.addAll(chatMessageRepository.findTop100ByTypeOrderByCreatedAtAsc("GENERAL"));
        result.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        return result;
    }

    @GetMapping("/api/messages/region")
    @ResponseBody
    public List<ChatMessage> getRegionMessages(@RequestParam String region) {
        List<ChatMessage> result = new ArrayList<>();
        result.addAll(chatMessageRepository.findTop100ByTypeOrderByCreatedAtAsc("JOIN"));
        result.addAll(chatMessageRepository.findTop100ByTypeOrderByCreatedAtAsc("LEAVE"));
        result.addAll(chatMessageRepository.findTop100ByTypeAndRegionOrderByCreatedAtAsc("REGION", region));
        result.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        return result;
    }

    @GetMapping("/api/messages/private")
    @ResponseBody
    public List<ChatMessage> getPrivateMessages(@RequestParam String username) {
        return chatMessageRepository.findTop100ByTypeAndUsernameOrTypeAndRecipientOrderByCreatedAtAsc(
                "PRIVATE",
                username,
                "PRIVATE",
                username
        );
    }

    private void sendStats() {
        Map<String, List<OnlineUser>> stats = new LinkedHashMap<>();

        String[] regions = {
                "Вінницька область", "Волинська область", "Дніпропетровська область",
                "Донецька область", "Житомирська область", "Закарпатська область",
                "Запорізька область", "Івано-Франківська область", "Київська область",
                "Кіровоградська область", "Луганська область", "Львівська область",
                "Миколаївська область", "Одеська область", "Полтавська область",
                "Рівненська область", "Сумська область", "Тернопільська область",
                "Харківська область", "Херсонська область", "Хмельницька область",
                "Черкаська область", "Чернівецька область", "Чернігівська область"
        };

        for (String region : regions) {
            stats.put(region, new ArrayList<>());
        }

        onlineUsers.forEach((username, user) -> {
            if (stats.containsKey(user.getRegion())) {
                stats.get(user.getRegion()).add(user);
            }
        });

        messagingTemplate.convertAndSend("/topic/stats", stats);
    }

    public static class OnlineUser {
        private String username;
        private String region;
        private String city;
        private String status;
        private Double latitude;
        private Double longitude;

        public OnlineUser(String username, String region, String city, String status, Double latitude, Double longitude) {
            this.username = username;
            this.region = region;
            this.city = city;
            this.status = status;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getUsername() {
            return username;
        }

        public String getRegion() {
            return region;
        }

        public String getCity() {
            return city;
        }

        public String getStatus() {
            return status;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    }
}