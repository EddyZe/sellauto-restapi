package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.UserProfileDto;
import ru.eddyz.sellautorestapi.entities.User;


@Mapper(componentModel = "spring", uses = {AccountMapper.class, PriceMapper.class})
public interface UserMapper {

    UserProfileDto toDto(User user);

}
