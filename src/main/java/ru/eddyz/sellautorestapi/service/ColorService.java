package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Color;
import ru.eddyz.sellautorestapi.exeptions.ColorException;
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

    public Color save(Color color) {
        if (colorRepository.findByTitle(color.getTitle()).isPresent()) {
            throw new ColorException("This color already exists");
        }
        return colorRepository.save(color);
    }

    public void deleteById(Integer colorId) {
        colorRepository.findById(colorId)
                .ifPresent(color -> colorRepository.deleteById(color.getColorId()));
    }
}
