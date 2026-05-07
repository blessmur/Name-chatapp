package com.example.chatapp;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/stats")
    public Map<String, Long> getRegionStats() {
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
            stats.put(region, userRepository.countByRegion(region));
        }

        return stats;
    }
}