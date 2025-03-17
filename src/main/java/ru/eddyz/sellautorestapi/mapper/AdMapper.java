package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.AdDto;
import ru.eddyz.sellautorestapi.entities.Ad;

@Mapper(componentModel = "spring")
public interface AdMapper {

    @InheritInverseConfiguration
    AdDto toDto(Ad ad);

    Ad toEntity(AdDto adDto);
}
