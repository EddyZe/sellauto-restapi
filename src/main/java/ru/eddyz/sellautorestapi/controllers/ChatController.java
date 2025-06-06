package ru.eddyz.sellautorestapi.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.dto.ChatBaseDto;
import ru.eddyz.sellautorestapi.dto.ChatMessagesDto;
import ru.eddyz.sellautorestapi.dto.ChatsDetailsDto;
import ru.eddyz.sellautorestapi.dto.MessageBaseDto;
import ru.eddyz.sellautorestapi.entities.Chat;
import ru.eddyz.sellautorestapi.entities.Message;
import ru.eddyz.sellautorestapi.exeptions.ChatException;
import ru.eddyz.sellautorestapi.exeptions.ChatNotFoundException;
import ru.eddyz.sellautorestapi.mapper.ChatMapper;
import ru.eddyz.sellautorestapi.mapper.MessageMapper;
import ru.eddyz.sellautorestapi.service.AdService;
import ru.eddyz.sellautorestapi.service.ChatService;
import ru.eddyz.sellautorestapi.service.MessageService;
import ru.eddyz.sellautorestapi.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
@Tag(name = "Чаты")
public class ChatController {

    private final ChatService chatService;
    private final AdService adService;
    private final UserService userService;
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;


    @PostMapping("/{adId}")
    @Operation(
            summary = "Создание чата",
            description = "Создает чат к объявлению с указанным id",
            parameters = {
                    @Parameter(name = "adId", description = "ID объявления")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            content = @Content(schema = @Schema(implementation = ChatBaseDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> createChat(@PathVariable Long adId, @AuthenticationPrincipal UserDetails userDetails) {
        var client = userService.findByEmail(userDetails.getUsername());
        var ad = adService.findById(adId);
        var seller = ad.getUser();

        if (userDetails.getUsername().equals(seller.getAccount().getEmail()))
            return ResponseEntity.badRequest()
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "You can't write to yourself."));

        for (Chat adChat : ad.getChats()) {
            for (Chat userChats : client.getChats()) {
                if (adChat.getChatId().equals(userChats.getChatId())) {
                    return ResponseEntity.ok(chatMapper.toDto(adChat));
                }
            }
        }

        var chat = Chat.builder()
                .ad(ad)
                .users(List.of(client, seller))
                .build();

        chat = chatService.save(chat);
        simpMessagingTemplate.convertAndSend("/topic/user/%d/chats".formatted(seller.getUserId()),
                chatMapper.toDto(chat));
        return ResponseEntity.status(HttpStatus.CREATED).body(chatMapper.toDto(chat));
    }

    @GetMapping("/my")
    @Operation(
            summary = "Список чатов текущего пользователя",
            description = "Отображает список текущего, авторизованного пользователя",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ChatsDetailsDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> getMyChats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ChatsDetailsDto.builder()
                                .chats(userService.findByEmail(userDetails.getUsername())
                                        .getChats()
                                        .stream()
                                        .sorted((o1, o2) ->
                                                o2.getMessages().getLast().getCreatedAt().compareTo(o1.getMessages().getLast().getCreatedAt()))
                                        .map(chatMapper::toDetailsDto)
                                        .toList())
                                .build()
                );
    }

    @GetMapping("/{chatId}")
    @Operation(
            summary = "Открыть чат",
            description = "Позволяет получить чат по его ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ChatsDetailsDto.class))
                    )
            }
    )
    public ResponseEntity<?> getChat(@PathVariable Long chatId, @AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByEmail(userDetails.getUsername());
        var chat = user.getChats()
                .stream()
                .filter(c -> c.getChatId().equals(chatId))
                .findFirst()
                .orElseThrow(() -> new ChatNotFoundException("The chat does not exist or you are not a member of it"));

        return ResponseEntity.ok(chatMapper.toDetailsDto(chat));
    }

    @GetMapping("/{chatId}/messages")
    @Operation(
            summary = "Сообщения",
            description = "Позволяет получить сообщения чата",
            parameters = {
                    @Parameter(name = "chatId", description = "ID чата")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ChatMessagesDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    public ResponseEntity<?> getMessages(@PathVariable Long chatId, @AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByEmail(userDetails.getUsername());
        var chat = user.getChats()
                .stream()
                .filter(c -> c.getChatId().equals(chatId))
                .findFirst()
                .orElseThrow(() -> new ChatException("You are not a member of this chat"));

        return ResponseEntity.ok(ChatMessagesDto
                .builder()
                .messages(chat.getMessages()
                        .stream()
                        .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                        .map(messageMapper::toDto)
                        .toList()
                )
                .build());
    }


    @PostMapping("/{chatId}/sendMessage")
    @Operation(
            summary = "Отправить сообщение",
            description = "Позволяет отправить сообщение в указанный чат",
            parameters = {
                    @Parameter(name = "chatId", description = "ID чата")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = MessageBaseDto.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            content = @Content(schema = @Schema(implementation = MessageBaseDto.class))
                    ),
                    @ApiResponse(
                            description = "Ошибка",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
                    )
            }
    )
    private ResponseEntity<?> sendMessage(@RequestBody MessageBaseDto message,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable Long chatId) {
        var chat = chatService.findById(chatId);
        var user = userService.findByEmail(userDetails.getUsername());

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
//        simpMessagingTemplate.convertAndSend("/app/chat/%d".formatted(chatId), message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(message);
    }
}
