package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.ColorBaseDto;
import ru.eddyz.sellautorestapi.entities.Color;

@Mapper(componentModel = "spring")
public interface ColorBaseMapper {
    ColorBaseDto toDto(Color color);
}
