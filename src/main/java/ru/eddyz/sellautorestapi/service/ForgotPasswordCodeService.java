package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.ForgotPasswordCode;
import ru.eddyz.sellautorestapi.entities.User;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.exeptions.ForgotCodeNotFoundException;
import ru.eddyz.sellautorestapi.repositories.AccountRepository;
import ru.eddyz.sellautorestapi.repositories.ForgotPasswordCodeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordCodeService {

    private final ForgotPasswordCodeRepository forgotPasswordCodeRepository;
    private final AccountRepository accountRepository;


    @Transactional
    public ForgotPasswordCode findCode(String email, String code) {
        return forgotPasswordCodeRepository.findByCodeAndUserEmail(code, email)
                .orElseThrow(() -> new ForgotCodeNotFoundException(code + " not found"));
    }

    public List<ForgotPasswordCode> findExpiredCodes(LocalDateTime currentDateTime) {
        return forgotPasswordCodeRepository.findExpiredCodes(currentDateTime);
    }

    public List<ForgotPasswordCode> findByActiveCodes(Boolean active) {
        return forgotPasswordCodeRepository.findByActive(active);
    }


    @Transactional
    public List<ForgotPasswordCode> findByActiveAndExpired(Boolean active, LocalDateTime currentDateTime) {
        List<ForgotPasswordCode> result = new ArrayList<>();
        result.addAll(findByActiveCodes(active));
        result.addAll(findExpiredCodes(currentDateTime));
        return result;
    }


    public boolean codeIsActive(ForgotPasswordCode code) {
        var currentDate =  LocalDateTime.now();
        if (code.getExpiredAt().isBefore(currentDate) || !code.getActive()) {
            forgotPasswordCodeRepository.deleteById(code.getId());
            return false;
        }
        return true;
    }

    public void deleteById(Long id) {
        if (forgotPasswordCodeRepository.findById(id).isEmpty()) {
            throw new ForgotCodeNotFoundException("Code not found");
        }
        forgotPasswordCodeRepository.deleteById(id);
    }

    public void editStatus(Long codeId, boolean isActive) {
        forgotPasswordCodeRepository.findById(codeId)
                .ifPresent(c -> {
                    c.setActive(isActive);
                    forgotPasswordCodeRepository.save(c);
                });
    }

    @Transactional
    public ForgotPasswordCode generateAndSaveCode(String email) {
        User user = getUser(email);
        String code;
        do {
            code = generateCode();
        } while (forgotPasswordCodeRepository.findByCodeAndUserEmail(code, email).isPresent());

        var currentDate = LocalDateTime.now();

        return forgotPasswordCodeRepository.save(
                ForgotPasswordCode.builder()
                        .user(user)
                        .createdAt(currentDate)
                        .active(true)
                        .expiredAt(currentDate.plusMinutes(10))
                        .code(code)
                        .build()
        );
    }

    private User getUser(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User not found"))
                .getUser();
    }


    private String generateCode() {
        Random random = new Random();
        return "%04d".formatted(random.nextInt(10000));
    }
}
