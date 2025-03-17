package ru.eddyz.sellautorestapi.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import ru.eddyz.sellautorestapi.dto.AccountDto;
import ru.eddyz.sellautorestapi.entities.Account;


@Mapper(componentModel = "spring")
public interface AccountMapper {

    @InheritInverseConfiguration
    AccountDto toDto(Account account);

    Account toEntity(AccountDto accountDto);
}
