package hu.progmasters.webshop.controller;

import hu.progmasters.webshop.dto.incoming.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<String, String> userReplyMap = new ConcurrentHashMap<>();

    private static final String ADMIN_EMAIL = "middle3arthmarket@gmail.com";

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        String username = principal.getName();
        chatMessage.setSender(username);

        sendAutomaticResponse(username);

        if (username.equals(ADMIN_EMAIL)) {
            chatMessage.setRecipient(userReplyMap.getOrDefault(username, ""));
        } else {
            chatMessage.setRecipient(ADMIN_EMAIL);
            userReplyMap.put(ADMIN_EMAIL, username);
        }

        if (!chatMessage.getRecipient().isEmpty()) {
            messagingTemplate.convertAndSendToUser(chatMessage.getRecipient(), "/queue/messages", chatMessage);
        } else {
            log.warn("Recipient not found for message: {}", chatMessage.getContent());
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor,
                        Principal principal) {

        String username = principal.getName();
        chatMessage.setSender(username);

        chatMessage.setRecipient(ADMIN_EMAIL);

        messagingTemplate.convertAndSendToUser(ADMIN_EMAIL, "/queue/messages", chatMessage);
    }

    public void sendAutomaticResponse(String recipient) {

        LocalTime currentTime = LocalTime.now();
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(8, 0);

        if (currentTime.isAfter(start) || currentTime.isBefore(end)) {
            ChatMessage autoReply = new ChatMessage();
            autoReply.setSender("System");
            autoReply.setContent("We received your question. It will be answered at opening hours (08:00).");
            messagingTemplate.convertAndSendToUser(recipient, "/queue/messages", autoReply);
        }
    }

    @GetMapping("/chat")
    public String chat() {
        return "forward:/chat.html";
    }
}
