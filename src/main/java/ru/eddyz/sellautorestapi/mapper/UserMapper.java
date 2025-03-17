package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.eddyz.sellautorestapi.dto.UserDto;
import ru.eddyz.sellautorestapi.entities.User;


@Mapper(componentModel = "spring", uses = {AccountMapper.class, AdMapper.class})
public interface UserMapper {

    @Mappings({
            @Mapping(source = "ads", target = "ads"),
            @Mapping(source = "account", target = "account")
    })
    UserDto toDto(User user);

    @Mappings({
            @Mapping(source = "ads", target = "ads"),
            @Mapping(source = "account", target = "account")
    })
    User toEntity(UserDto userDto);
}
