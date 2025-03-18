package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.PriceBaseDto;
import ru.eddyz.sellautorestapi.entities.Price;

@Mapper(componentModel = "spring")
public interface PriceMapper {
    PriceBaseDto toDto(Price price);
}
