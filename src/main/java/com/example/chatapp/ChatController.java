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

    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();
    private final Map<String, String> userStatuses = new ConcurrentHashMap<>();

    @MessageMapping("/join")
    public void join(ChatMessage message) {
        onlineUsers.put(message.getUsername(), message.getRegion());
        userStatuses.put(message.getUsername(), message.getStatus() != null ? message.getStatus() : "ONLINE");

        ChatMessage joinMessage = new ChatMessage(
                "System",
                message.getUsername() + " приєднався до чату з області: " + message.getRegion(),
                message.getRegion(),
                "JOIN"
        );

        chatMessageRepository.save(joinMessage);
        messagingTemplate.convertAndSend("/topic/messages", joinMessage);
        sendStats();
    }

    @MessageMapping("/leave")
    public void leave(ChatMessage message) {
        onlineUsers.remove(message.getUsername());
        userStatuses.remove(message.getUsername());

        ChatMessage leaveMessage = new ChatMessage(
                "System",
                message.getUsername() + " вийшов з чату",
                message.getRegion(),
                "LEAVE"
        );

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
        userStatuses.put(message.getUsername(), message.getStatus());
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

        onlineUsers.forEach((username, region) -> {
            if (stats.containsKey(region)) {
                String status = userStatuses.getOrDefault(username, "ONLINE");
                stats.get(region).add(new OnlineUser(username, status));
            }
        });

        messagingTemplate.convertAndSend("/topic/stats", stats);
    }

    public static class OnlineUser {
        private String username;
        private String status;

        public OnlineUser(String username, String status) {
            this.username = username;
            this.status = status;
        }

        public String getUsername() {
            return username;
        }

        public String getStatus() {
            return status;
        }
    }
}