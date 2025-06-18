package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<ChatMessage> findBySenderOrderByCreatedAtDesc(String sender);
    List<ChatMessage> findTop100ByOrderByCreatedAtDesc();
    List<ChatMessage> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT c.sender) FROM ChatMessage c")
    long countDistinctSender();
}
