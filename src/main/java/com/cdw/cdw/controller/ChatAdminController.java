package com.cdw.cdw.controller;
import com.cdw.cdw.domain.entity.ChatMessage;
import com.cdw.cdw.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/chat")
public class ChatAdminController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/messages")
    public ResponseEntity<Page<ChatMessage>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ChatMessage> messages = chatMessageRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/user/{username}")
    public ResponseEntity<List<ChatMessage>> getUserMessages(@PathVariable String username) {
        List<ChatMessage> messages = chatMessageRepository.findBySenderOrderByCreatedAtDesc(username);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/date")
    public ResponseEntity<List<ChatMessage>> getMessagesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<ChatMessage> messages = chatMessageRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getChatStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalMessages = chatMessageRepository.count();
        long totalUsers = chatMessageRepository.countDistinctSender();
        long todayMessages = chatMessageRepository.countByCreatedAtBetween(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        );

        stats.put("totalMessages", totalMessages);
        stats.put("totalUsers", totalUsers);
        stats.put("todayMessages", todayMessages);

        return ResponseEntity.ok(stats);
    }
}
