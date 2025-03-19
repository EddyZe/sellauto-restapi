package ru.eddyz.sellautorestapi.payloads;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatListPayload {

    private Long chatId;
    private String title;
    private String lastMessage;
    private Long adId;
    private List<MessagePayload> messages;
    private List<String> userEmails;
}
