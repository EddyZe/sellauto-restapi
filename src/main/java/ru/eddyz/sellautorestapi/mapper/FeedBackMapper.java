package ru.eddyz.sellautorestapi.mapper;


import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.FeedBackDto;
import ru.eddyz.sellautorestapi.entities.FeedBack;

@Mapper(componentModel = "spring", uses = {UserBaseMapper.class})
public interface FeedBackMapper {

    FeedBackDto toDto(FeedBack feedBack);
}
