package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.Account;
import ru.eddyz.sellautorestapi.enums.Role;
import ru.eddyz.sellautorestapi.exeptions.AccountException;
import ru.eddyz.sellautorestapi.repositories.AccountRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public Account createAccount(Account account) {
        if (account.getPassword() == null || account.getPassword().isBlank()) {
            throw new AccountException("Password is required", "PASSWORD_REQUIRED");
        }

        checkEmailAndPhoneNumber(account);
        return accountRepository.save(account
                .toBuilder()
                .password(passwordEncoder.encode(account.getPassword()))
                .role(Role.ROLE_USER)
                .blocked(false)
                .build());
    }

    public Optional<Account> findByPhoneNumber(String phoneNumber) {
        return accountRepository.findByPhoneNumber(phoneNumber);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public void update(Account account) {
        accountRepository.save(account);
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }


    @Transactional
    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByEmail(username)
                .map(this::buildUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public UserDetails buildUserDetails(Account account) {
        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .accountLocked(account.isBlocked())
                .accountExpired(false)
                .authorities(Collections.singleton(new SimpleGrantedAuthority(account.getRole().toString())))
                .disabled(false)
                .build();
    }

    private void checkEmailAndPhoneNumber(Account account) {
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            throw new AccountException("Email is already", "ACCOUNT_ALREADY_EXISTS");
        }

        if (accountRepository.findByPhoneNumber(account.getPhoneNumber()).isPresent()) {
            throw new AccountException("Phone number is already", "ACCOUNT_ALREADY_EXISTS");
        }
    }
}
