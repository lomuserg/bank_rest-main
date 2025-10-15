package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.repository.jpa.CardsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CardsRepository {

    private final CardsJpaRepository cardsJpaRepository;

    public Card save(Card card) {
        return cardsJpaRepository.save(card);
    }

    public boolean existsByCardHash(String cardHash) {
        return cardsJpaRepository.existsByCardHash(cardHash);
    }

    public Card findById(Long id) {
        return cardsJpaRepository.findById(id)
                .orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
    }

    public Card findByCardNumber(String cardNumber) {
        return cardsJpaRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
    }

    public List<Card> findAllByOwnerId(Long ownerId) {
        return cardsJpaRepository.findAllByOwnerId(ownerId);
    }

    public Page<Card> findPageByOwnerId(Long ownerId, Pageable pageable) {
        return cardsJpaRepository.findAllByOwnerId(ownerId, pageable);
    }

    public List<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status) {
        return cardsJpaRepository.findAllByOwnerIdAndStatus(ownerId, status);
    }

    public Page<Card> findPageByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable) {
        return cardsJpaRepository.findAllByOwnerIdAndStatus(ownerId, status, pageable);
    }

    public Page<Card> findAllByStatus(CardStatus status, Pageable pageable) {
        return cardsJpaRepository.findAllByStatus(status, pageable);
    }

    public Page<Card> findAll(Pageable pageable) {
        return cardsJpaRepository.findAll(pageable);
    }

    public List<Card> findAll() {
        return cardsJpaRepository.findAll();
    }

    public void deleteById(Long id) {
        Card card = findById(id);
        cardsJpaRepository.deleteById(card.getId());
    }

    public Card findByIdWithLock(Long id) {
        return cardsJpaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new AppException("Card not found", HttpStatus.NOT_FOUND));
    }
}
