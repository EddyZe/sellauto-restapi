package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.BrandBaseDto;
import ru.eddyz.sellautorestapi.dto.BrandDetailsDto;
import ru.eddyz.sellautorestapi.entities.Brand;

@Mapper(componentModel = "spring", uses = {ModelsBaseMapper.class})
public interface BrandBaseMapper {

    BrandBaseDto toDto(Brand brand);

    BrandDetailsDto toDetailsDto(Brand brand);
}
