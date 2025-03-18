package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.BrandBaseDto;
import ru.eddyz.sellautorestapi.entities.Model;

@Mapper(componentModel = "spring", uses = {ModelsBaseMapper.class})
public interface BrandBaseMapper {

    BrandBaseDto toDto(Model model);
}
