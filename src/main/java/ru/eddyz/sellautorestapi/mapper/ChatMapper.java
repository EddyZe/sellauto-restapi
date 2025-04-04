package ru.eddyz.sellautorestapi.mapper;

import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.ChatBaseDto;
import ru.eddyz.sellautorestapi.dto.ChatDetailsDto;
import ru.eddyz.sellautorestapi.entities.Chat;

@Mapper(componentModel = "spring", uses = {MessageMapper.class, UserMapper.class,  AdDetailsMapper.class})
public interface ChatMapper {

    ChatBaseDto toDto(Chat chat);
    ChatDetailsDto toDetailsDto (Chat chat);


}
