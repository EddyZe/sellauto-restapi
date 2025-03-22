package ru.eddyz.sellautorestapi.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.eddyz.sellautorestapi.dto.MessageBaseDto;
import ru.eddyz.sellautorestapi.entities.Message;
import ru.eddyz.sellautorestapi.exeptions.ChatException;
import ru.eddyz.sellautorestapi.mapper.MessageMapper;
import ru.eddyz.sellautorestapi.service.ChatService;
import ru.eddyz.sellautorestapi.service.MessageService;
import ru.eddyz.sellautorestapi.service.UserService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class WebSocketChatController {

    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat/{chatId}")
    public List<MessageBaseDto> messages(
            @DestinationVariable Long chatId,
            MessageBaseDto message, Principal principal) {

        var chat = chatService.findById(chatId);
        var user = userService.findByEmail(principal.getName());

        if (chat.getUsers().stream().noneMatch(u ->
                u.getUserId().equals(user.getUserId()))) {
            throw new ChatException("You are not a member of the chat");
        }


        message = messageMapper.toDto(
                messageService.save(Message.builder()
                        .chat(chat)
                        .message(message.getMessage())
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
