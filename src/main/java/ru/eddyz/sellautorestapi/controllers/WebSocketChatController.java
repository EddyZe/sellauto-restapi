package ru.eddyz.sellautorestapi.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.eddyz.sellautorestapi.dto.MessageBaseDto;
import ru.eddyz.sellautorestapi.entities.Message;
import ru.eddyz.sellautorestapi.exeptions.ChatException;
import ru.eddyz.sellautorestapi.mapper.MessageMapper;
import ru.eddyz.sellautorestapi.security.JwtService;
import ru.eddyz.sellautorestapi.service.ChatService;
import ru.eddyz.sellautorestapi.service.MessageService;
import ru.eddyz.sellautorestapi.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class WebSocketChatController {

    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate template;
    private final JwtService jwtService;

    @Transactional
    @MessageMapping("/chat/{chatId}/{token}")
    public List<MessageBaseDto> messages(
            @DestinationVariable("chatId") Long chatId,
            @DestinationVariable("token") String jwtToken,
            String text) {

        var email = jwtService.extractEmail(jwtToken);
        var chat = chatService.findById(chatId);
        var user = userService.findByEmail(email);

        if (chat.getUsers().stream().noneMatch(u ->
                u.getUserId().equals(user.getUserId()))) {
            throw new ChatException("You are not a member of the chat");
        }

        var message = messageMapper.toDto(
                messageService.save(Message.builder()
                        .chat(chat)
                        .message(text)
                        .createdAt(LocalDateTime.now())
                        .senderName(user.getFirstName())
                        .from(user)
                        .build()
                ));

        template.convertAndSend("/topic/chat/%d/messages".formatted(chatId), message);
        return chat.getMessages()
                .stream()
                .map(messageMapper::toDto)
                .toList();
    }
}
