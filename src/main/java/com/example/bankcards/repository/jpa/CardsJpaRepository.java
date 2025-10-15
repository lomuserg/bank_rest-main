package com.example.bankcards.repository.jpa;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardsJpaRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findAllByOwnerId(Long ownerId);
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    List<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status);
    Page<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);

    Page<Card> findAll(Pageable pageable);

    boolean existsByCardHash(String cardHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdWithLock(@Param("id") Long id);
}
