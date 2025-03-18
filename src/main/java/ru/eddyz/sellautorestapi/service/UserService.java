package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.User;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

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

    public void update(User user) {
        if (userRepository.findById(user.getUserId()).isEmpty())
            throw new UsernameNotFoundException("User not found");

        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByAccountEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User not found"));
    }

    public void deleteById(Long id) {
        if (userRepository.findById(id).isEmpty())
            throw new UsernameNotFoundException("User not found");

        userRepository.deleteById(id);
    }

    @Transactional
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public Page<User> findByFirstName(String firstName, Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
