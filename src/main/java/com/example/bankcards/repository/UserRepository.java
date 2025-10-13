package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable);
    }

    public User findById(Long id) {
        return userJpaRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
    }

    public User getByLogin(String login) {
        return userJpaRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
    }

    public Optional<User> findByLogin(String login) {
        return userJpaRepository.findByLogin(login);
    }

    public boolean existsByLogin(String login) {
        return userJpaRepository.existsByLogin(login);
    }

    public void deleteById(Long id) {
        if (!userJpaRepository.existsById(id)) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }
        userJpaRepository.deleteById(id);
    }

    public void updateBannedStatus(Long id, boolean banned) {
        User user = findById(id);
        user.setBanned(banned);
        userJpaRepository.save(user);
    }

}
