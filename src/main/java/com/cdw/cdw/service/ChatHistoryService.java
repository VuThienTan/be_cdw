package com.cdw.cdw.service;

import com.cdw.cdw.domain.entity.ChatMessage;
import com.cdw.cdw.domain.enums.MessageType;
import com.cdw.cdw.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public void saveMessage(ChatMessage chatMessage, String sessionId) {
        chatMessage.setSessionId(sessionId);
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistoryBySession(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public List<ChatMessage> getRecentChatHistory() {
        return chatMessageRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
