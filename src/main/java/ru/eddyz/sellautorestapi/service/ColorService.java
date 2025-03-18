package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Color;
import ru.eddyz.sellautorestapi.exeptions.ColorNotFoundException;
import ru.eddyz.sellautorestapi.repositories.ColorRepository;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;

    public Color findByTitle(String title) {
        return colorRepository.findByTitle(title)
                .orElseThrow(() -> new ColorNotFoundException("Color not found"));
    }
}
