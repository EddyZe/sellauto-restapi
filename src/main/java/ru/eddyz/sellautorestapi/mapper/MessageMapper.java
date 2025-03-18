package ru.eddyz.sellautorestapi.mapper;

import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.MessageBaseDto;
import ru.eddyz.sellautorestapi.entities.Message;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MessageMapper {

    MessageBaseDto toDto(Message message);

}
