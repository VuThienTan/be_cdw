package com.cdw.cdw.controller;

import com.cdw.cdw.domain.entity.ChatMessage;
import com.cdw.cdw.domain.enums.MessageType;
import com.cdw.cdw.service.AIChatService;
import com.cdw.cdw.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String sessionId = headerAccessor.getSessionId();

        // Xử lý tin nhắn người dùng và tạo phản hồi
        if (chatMessage.getType() == MessageType.CHAT) {
            // Lưu tin nhắn của người dùng (được xử lý trong aiChatService)
            String botResponse = aiChatService.processMessage(
                    chatMessage.getContent(),
                    username,
                    sessionId
            );

            // Tạo và gửi phản hồi từ bot
            ChatMessage botMessage = new ChatMessage();
            botMessage.setContent(botResponse);
            botMessage.setSender("AI Assistant");
            botMessage.setType(MessageType.BOT);
            botMessage.setTimestamp(System.currentTimeMillis());

            return botMessage;
        }

        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Lưu username trong WebSocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // Lưu tin nhắn tham gia
        chatHistoryService.saveMessage(chatMessage, headerAccessor.getSessionId());

        return chatMessage;
    }

    // API để lấy lịch sử chat
    @GetMapping("/api/chat/history/{sessionId}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable String sessionId) {
        return chatHistoryService.getChatHistoryBySession(sessionId);
    }

    @GetMapping("/api/chat/history/recent")
    @ResponseBody
    public List<ChatMessage> getRecentChatHistory() {
        return chatHistoryService.getRecentChatHistory();
    }
}
