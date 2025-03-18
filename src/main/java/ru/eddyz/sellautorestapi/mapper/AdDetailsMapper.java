package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.AdDetailsDto;
import ru.eddyz.sellautorestapi.entities.Ad;

@Mapper(componentModel = "spring", uses = {CarDetailsMapper.class, ChatBaseMapper.class, UserBaseMapper.class, PriceMapper.class})
public interface AdDetailsMapper {
    AdDetailsDto toDto(Ad ad);
}
