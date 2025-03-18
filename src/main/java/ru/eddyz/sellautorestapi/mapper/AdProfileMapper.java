package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.AdProfileDto;
import ru.eddyz.sellautorestapi.entities.Ad;

@Mapper(componentModel = "spring", uses = {ChatBaseMapper.class, UserMapper.class, CarProfileMapper.class})
public interface AdProfileMapper {

    AdProfileDto toDto(Ad ad);

}
