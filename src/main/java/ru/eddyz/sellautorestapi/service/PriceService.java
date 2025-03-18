package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Price;
import ru.eddyz.sellautorestapi.repositories.PriceRepository;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository priceRepository;


    public Price save(Price price) {
        return priceRepository.save(price);
    }
}
