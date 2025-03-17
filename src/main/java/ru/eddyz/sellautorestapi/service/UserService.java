package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.eddyz.sellautorestapi.entities.User;
import ru.eddyz.sellautorestapi.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User save(String firstName, String lastName) {
        return userRepository.save(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build());
    }
}
