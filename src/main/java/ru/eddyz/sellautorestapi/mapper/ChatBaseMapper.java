package ru.eddyz.sellautorestapi.mapper;

import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.ChatBaseDto;
import ru.eddyz.sellautorestapi.entities.Chat;

@Mapper(componentModel = "spring", uses = {MessageMapper.class, UserMapper.class})
public interface ChatBaseMapper {

    ChatBaseDto toDto(Chat chat);

}
