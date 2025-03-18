package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.UserBaseDto;
import ru.eddyz.sellautorestapi.dto.UserProfileDto;
import ru.eddyz.sellautorestapi.entities.User;

@Mapper(componentModel = "spring")
public interface UserBaseMapper {
    UserBaseDto toDto(User user);
}
