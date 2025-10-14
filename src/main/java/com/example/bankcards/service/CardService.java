package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardsRepository;
import com.example.bankcards.util.CardNumberCryptoUtil;
import com.example.bankcards.util.DebitCardGenerator;
import com.example.bankcards.util.HashUtil;
import com.example.bankcards.util.MaskingUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardsRepository cardsRepository;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final DebitCardGenerator debitCardGenerator;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private final CardNumberCryptoUtil cryptoUtil;

    public CardDto createCard(CreateCardRequest request) {
        User owner = userService.getUserById(request.getOwnerId());

        String cardNumber = generateUniqueCardNumber();
        String encryptedCardNumber = cryptoUtil.encrypt(cardNumber);

        Card card = Card.builder()
                .cardNumber(encryptedCardNumber)
                .cardHash(HashUtil.sha256(cardNumber))
                .owner(owner)
                .expiryDate(request.getExpiryDate())
                .status(CardStatus.ACTIVE)
                .balance(getBalanceOrDefault(request.getBalance()))
                .build();

        Card saved = cardsRepository.save(card);

        CardDto dto = cardMapper.toDto(saved);
        dto.setMaskedCardNumber(MaskingUtil.maskCardNumber(cardNumber));
        return dto;
    }

    private String generateUniqueCardNumber() {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String cardNumber = debitCardGenerator.generateCardNumber();
            String cardHash = HashUtil.sha256(cardNumber);

            if (!cardsRepository.existsByCardHash(cardHash)) {
                return cardNumber;
            }
        }
        throw new RuntimeException("Не удалось сгенерировать уникальный номер карты после " + MAX_GENERATION_ATTEMPTS + " попыток");
    }

    private boolean isCardNumberUnique(String cardNumber) {
        String cardHash = HashUtil.sha256(cardNumber);
        return !cardsRepository.existsByCardHash(cardHash);
    }

    private BigDecimal getBalanceOrDefault(BigDecimal balance) {
        return balance != null ? balance : BigDecimal.ZERO;
    }

    // 🔹 Пользователь: Получить свои карты (с фильтрацией по статусу)
    public Page<CardDto> getUserCards(String login, CardStatus status, Pageable pageable) {
        User user = userService.getUserByLogin(login);
        Page<Card> cards = status != null
                ? cardsRepository.findPageByOwnerIdAndStatus(user.getId(), status, pageable)
                : cardsRepository.findPageByOwnerId(user.getId(), pageable);
        return cards.map(cardMapper::toDto);
    }

    // 🔹 Пользователь: Запрос на блокировку своей карты
    public void requestBlockCard(Long cardId, String login) {
        Card card = getCardAndCheckOwnership(cardId, login);

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new AppException("Карта не активна", HttpStatus.BAD_REQUEST);
        }

        // Можно реализовать как заявку, но пока сразу устанавливаем статус
        card.setStatus(CardStatus.BLOCKED);
        cardsRepository.save(card);
    }

    // 🔹 Пользователь: Перевод между своими картами
    @Transactional
    public void transferBetweenOwnCards(Long fromId, Long toId, BigDecimal amount, String login) {
        Card from = getCardAndCheckOwnership(fromId, login);
        Card to = getCardAndCheckOwnership(toId, login);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new AppException("Недостаточно средств", HttpStatus.BAD_REQUEST);
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardsRepository.save(from);
        cardsRepository.save(to);
    }

    // 🔹 Пользователь: Просмотр баланса карты
    public BigDecimal getBalance(Long cardId, String login) {
        Card card = getCardAndCheckOwnership(cardId, login);
        return card.getBalance();
    }

    // 🔹 Админ: Все карты (с фильтрацией)
    public Page<CardDto> getAllCards(CardStatus status, Pageable pageable) {
        Page<Card> cards = status != null
                ? cardsRepository.findAllByStatus(status, pageable)
                : cardsRepository.findAll(pageable);
        return cards.map(cardMapper::toDto);
    }

    // 🔹 Админ: Принудительная блокировка
    public void blockCard(Long id) {
        Card card = findCardById(id);
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new AppException("Карта уже заблокирована", HttpStatus.CONFLICT);
        }
        card.setStatus(CardStatus.BLOCKED);
        cardsRepository.save(card);
    }

    // 🔹 Админ: Активация (разблокировка)
    public void activateCard(Long id) {
        Card card = findCardById(id);
        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new AppException("Карта уже активна", HttpStatus.CONFLICT);
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new AppException("Нельзя активировать просроченную карту", HttpStatus.BAD_REQUEST);
        }
        card.setStatus(CardStatus.ACTIVE);
        cardsRepository.save(card);
    }

    // 🔹 Админ: Удаление карты
    public void deleteCard(Long id) {
        Card card = findCardById(id);
        cardsRepository.deleteById(card.getId());
    }

    // 🔹 Вспомогательные методы

    private Card getCardAndCheckOwnership(Long cardId, String login) {
        Card card = findCardById(cardId);
        User owner = userService.getUserByLogin(login);
        if (!card.getOwner().getId().equals(owner.getId())) {
            throw new AppException("Доступ запрещён", HttpStatus.FORBIDDEN);
        }
        return card;
    }

    private Card findCardById(Long id) {
        return cardsRepository.findById(id);
    }

}
