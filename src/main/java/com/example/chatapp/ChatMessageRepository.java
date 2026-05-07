package com.example.chatapp;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop100ByTypeOrderByCreatedAtAsc(String type);

    List<ChatMessage> findTop100ByTypeAndRegionOrderByCreatedAtAsc(String type, String region);

    List<ChatMessage> findTop100ByTypeAndUsernameOrTypeAndRecipientOrderByCreatedAtAsc(
            String type1,
            String username,
            String type2,
            String recipient
    );
}