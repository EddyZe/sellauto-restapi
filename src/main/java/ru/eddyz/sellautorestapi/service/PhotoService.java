package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.Photo;
import ru.eddyz.sellautorestapi.exeptions.PhotoNotFoundException;
import ru.eddyz.sellautorestapi.repositories.PhotoRepository;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository photoRepository;


    public Photo findById(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo with id: " + photoId + " not found!"));
    }

    public Photo save(Photo photo) {
        return photoRepository.save(photo);
    }
}
