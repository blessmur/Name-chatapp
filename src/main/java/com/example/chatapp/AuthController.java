package com.example.chatapp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {
        String username = normalize(request.getUsername());
        String password = request.getPassword();

        if (username.isBlank() || password == null || password.length() < 4) {
            return new AuthResponse(false, "Ім’я або пароль некоректні. Пароль має бути мінімум 4 символи.");
        }

        if (userRepository.existsByUsername(username)) {
            return new AuthResponse(false, "Користувач з таким ім’ям вже існує.");
        }

        String hash = passwordEncoder.encode(password);

        User user = new User(
                username,
                hash,
                request.getRegion() != null ? request.getRegion() : "Запорізька область",
                request.getCity() != null ? request.getCity() : "Запоріжжя",
                request.getLatitude() != null ? request.getLatitude() : 47.8388,
                request.getLongitude() != null ? request.getLongitude() : 35.1396
        );

        userRepository.save(user);

        return new AuthResponse(true, "Реєстрація успішна.", user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        String username = normalize(request.getUsername());
        String password = request.getPassword();

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (passwordEncoder.matches(password, user.getPasswordHash())) {
                        return new AuthResponse(true, "Вхід успішний.", user);
                    }

                    return new AuthResponse(false, "Невірний пароль.");
                })
                .orElseGet(() -> new AuthResponse(false, "Користувача не знайдено."));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}