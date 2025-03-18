package ru.eddyz.sellautorestapi.mapper;

import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.AccountProfileDto;
import ru.eddyz.sellautorestapi.entities.Account;


@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountProfileDto toDto(Account account);

}
