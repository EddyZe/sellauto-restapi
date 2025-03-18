package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.ModelBaseDto;
import ru.eddyz.sellautorestapi.entities.Model;

@Mapper(componentModel = "spring")
public interface ModelsBaseMapper {
    ModelBaseDto toDto(Model model);
}
