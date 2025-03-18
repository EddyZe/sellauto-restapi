package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.CarDetailsDto;
import ru.eddyz.sellautorestapi.entities.Car;

@Mapper(componentModel = "spring", uses = {PriceMapper.class, BrandBaseMapper.class, ModelsBaseMapper.class, ColorBaseMapper.class})
public interface CarDetailsMapper {
    CarDetailsDto toDto(Car car);
}
