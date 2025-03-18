package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.CarProfileDto;
import ru.eddyz.sellautorestapi.entities.Car;

@Mapper(componentModel = "spring", uses = PriceMapper.class)
public interface CarProfileMapper {
    CarProfileDto toDto(Car car);
}
