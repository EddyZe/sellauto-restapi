package ru.eddyz.sellautorestapi.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.service.ForgotPasswordCodeService;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class ForgotCodePasswordScheduler {

    private final ForgotPasswordCodeService forgotPasswordCodeService;

    @Scheduled(fixedDelay = 20, timeUnit = TimeUnit.MINUTES)
    public void removeIsNotActiveAndExpiredCodes() {
        forgotPasswordCodeService.findByActiveAndExpired(
                        false,
                        LocalDateTime.now()
                )
                .forEach(code -> {
                    if (!forgotPasswordCodeService.codeIsActive(code)) {
                        forgotPasswordCodeService.deleteById(code.getId());
                    }
                });
    }
}
