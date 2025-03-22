package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.Chat;
import ru.eddyz.sellautorestapi.exeptions.ChatNotFoundException;
import ru.eddyz.sellautorestapi.repositories.ChatRepository;
import ru.eddyz.sellautorestapi.repositories.MessageRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;


    public Chat save(Chat chat) {
        return chatRepository.save(chat);
    }

    @Transactional
    public Chat findById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
    }
}
