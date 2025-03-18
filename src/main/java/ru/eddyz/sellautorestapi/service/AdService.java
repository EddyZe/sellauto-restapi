package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Ad;
import ru.eddyz.sellautorestapi.repositories.AdRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;


    public List<Ad> findUserAdsByUserId(Long userId) {
        return adRepository.findAdUser(userId);
    }

    public List<Ad> findByAdsByUserEmail(String email) {
        return adRepository.findAdUserByEmail(email);
    }
}
