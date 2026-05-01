package com.example.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/stats")
    public Map<String, Long> getRegionStats() {
        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put("Вінницька область", userRepository.countByRegion("Вінницька область"));
        stats.put("Волинська область", userRepository.countByRegion("Волинська область"));
        stats.put("Дніпропетровська область", userRepository.countByRegion("Дніпропетровська область"));
        stats.put("Донецька область", userRepository.countByRegion("Донецька область"));
        stats.put("Житомирська область", userRepository.countByRegion("Житомирська область"));
        stats.put("Закарпатська область", userRepository.countByRegion("Закарпатська область"));
        stats.put("Запорізька область", userRepository.countByRegion("Запорізька область"));
        stats.put("Івано-Франківська область", userRepository.countByRegion("Івано-Франківська область"));
        stats.put("Київська область", userRepository.countByRegion("Київська область"));
        stats.put("Кіровоградська область", userRepository.countByRegion("Кіровоградська область"));
        stats.put("Луганська область", userRepository.countByRegion("Луганська область"));
        stats.put("Львівська область", userRepository.countByRegion("Львівська область"));
        stats.put("Миколаївська область", userRepository.countByRegion("Миколаївська область"));
        stats.put("Одеська область", userRepository.countByRegion("Одеська область"));
        stats.put("Полтавська область", userRepository.countByRegion("Полтавська область"));
        stats.put("Рівненська область", userRepository.countByRegion("Рівненська область"));
        stats.put("Сумська область", userRepository.countByRegion("Сумська область"));
        stats.put("Тернопільська область", userRepository.countByRegion("Тернопільська область"));
        stats.put("Харківська область", userRepository.countByRegion("Харківська область"));
        stats.put("Херсонська область", userRepository.countByRegion("Херсонська область"));
        stats.put("Хмельницька область", userRepository.countByRegion("Хмельницька область"));
        stats.put("Черкаська область", userRepository.countByRegion("Черкаська область"));
        stats.put("Чернівецька область", userRepository.countByRegion("Чернівецька область"));
        stats.put("Чернігівська область", userRepository.countByRegion("Чернігівська область"));

        return stats;
    }
}