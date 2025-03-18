package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.PhotoBaseDto;
import ru.eddyz.sellautorestapi.entities.Photo;

@Mapper(componentModel = "spring")
public interface PhotoBaseMapper {
    PhotoBaseDto toDto(Photo photo);
}
