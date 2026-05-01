package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    @MessageMapping("/join")
    public void join(ChatMessage message) {
        onlineUsers.put(message.getUsername(), message.getRegion());

        ChatMessage joinMessage = new ChatMessage(
                "System",
                message.getUsername() + " приєднався до чату з області: " + message.getRegion(),
                message.getRegion(),
                "JOIN"
        );

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

        messagingTemplate.convertAndSend("/topic/messages", leaveMessage);
        sendStats();
    }

    @MessageMapping("/send")
    public void sendMessage(ChatMessage message) {
        message.setType("CHAT");
        messagingTemplate.convertAndSend("/topic/messages", message);
    }

    private void sendStats() {
        Map<String, Long> stats = new LinkedHashMap<>();

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
            long count = onlineUsers.values().stream()
                    .filter(r -> r.equals(region))
                    .count();

            stats.put(region, count);
        }

        messagingTemplate.convertAndSend("/topic/stats", stats);
    }
}