package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserJpaRepository userJpaRepository;


    public User save(User user) {
        return userJpaRepository.save(user);
    }

    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    public User findById(Long id) {
        return userJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getByLogin(String login) {
        return userJpaRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
    }

    public Optional<User> findByLogin(String login){
        return userJpaRepository.findByLogin(login);
    }

}
