package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.dto.card.TransferRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardsRepository cardsRepository;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final DebitCardGenerator debitCardGenerator;
    private final CardNumberCryptoUtil cryptoUtil;

    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        User owner = userService.getUserById(request.getOwnerId());

        String cardNumber = debitCardGenerator.generateUniqueCardNumber();
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
        return convertToDtoWithMaskedNumber(saved);
    }

    private BigDecimal getBalanceOrDefault(BigDecimal balance) {
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Transactional
    public void activateCard(Long id) {
        Card card = findCardById(id);
        validateCardStatus(card, CardStatus.ACTIVE, "Card is already active");

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new AppException("Cannot activate an expired card", HttpStatus.BAD_REQUEST);
        }

        card.setStatus(CardStatus.ACTIVE);
        cardsRepository.save(card);
    }

    @Transactional
    public void blockCard(Long id) {
        Card card = findCardById(id);
        validateCardStatus(card, CardStatus.BLOCKED, "Card is already blocked");
        card.setStatus(CardStatus.BLOCKED);
        cardsRepository.save(card);
    }

    private void validateCardStatus(Card card, CardStatus forbiddenStatus, String errorMessage) {
        if (card.getStatus() == forbiddenStatus) {
            throw new AppException(errorMessage, HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = findCardById(id);
        cardsRepository.deleteById(card.getId());
    }

    private Card findCardById(Long id) {
        return cardsRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(CardStatus status, Pageable pageable) {
        Page<Card> cards = status != null
                ? cardsRepository.findAllByStatus(status, pageable)
                : cardsRepository.findAll(pageable);

        return convertToDtoPage(cards);
    }

    private Page<CardDto> convertToDtoPage(Page<Card> cards) {
        return cards.map(this::convertToDtoWithMaskedNumber);
    }

    private CardDto convertToDtoWithMaskedNumber(Card card) {
        CardDto dto = cardMapper.toDto(card);
        String decryptedNumber = cryptoUtil.decrypt(card.getCardNumber());
        dto.setMaskedCardNumber(MaskingUtil.maskCardNumber(decryptedNumber));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(String login, CardStatus status, Pageable pageable) {
        User user = userService.getUserByLogin(login);
        Page<Card> cards = status != null
                ? cardsRepository.findPageByOwnerIdAndStatus(user.getId(), status, pageable)
                : cardsRepository.findPageByOwnerId(user.getId(), pageable);

        return convertToDtoPage(cards);
    }

    @Transactional(readOnly = true)
    public List<CardDto> getUserCardsByLastFourDigits(String login, String lastFourDigits) {
        validateLastFourDigits(lastFourDigits);

        User user = userService.getUserByLogin(login);
        List<Card> userCards = cardsRepository.findAllByOwnerId(user.getId());

        return userCards.stream()
                .filter(card -> matchesLastFourDigits(card, lastFourDigits))
                .map(this::convertToDtoWithMaskedNumber)
                .collect(Collectors.toList());
    }

    private boolean matchesLastFourDigits(Card card, String lastFourDigits) {
        try {
            String decryptedCardNumber = cryptoUtil.decrypt(card.getCardNumber());
            return decryptedCardNumber.endsWith(lastFourDigits);
        } catch (Exception e) {
            return false;
        }
    }

    private void validateLastFourDigits(String lastFourDigits) {
        if (lastFourDigits == null || lastFourDigits.length() != 4) {
            throw new AppException("Last 4 digits must consist of 4 characters", HttpStatus.BAD_REQUEST);
        }

        if (!lastFourDigits.matches("\\d{4}")) {
            throw new AppException("Last 4 digits must contain only numbers", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void requestBlockCard(Long cardId, String login) {
        Card card = getCardAndValidateOwnership(cardId, login);
        validateCardStatus(card, CardStatus.ACTIVE, "Card is not active");

        card.setStatus(CardStatus.BLOCKED);
        cardsRepository.save(card);
    }

    private Card getCardAndValidateOwnership(Long cardId, String login) {
        Card card = findCardById(cardId);
        User owner = userService.getUserByLogin(login);

        if (!card.getOwner().getId().equals(owner.getId())) {
            throw new AppException("Card does not belong to the user", HttpStatus.FORBIDDEN);
        }
        return card;
    }

    private void validateCardsOwnership(Card card1, Card card2, User owner) {
        if (!card1.getOwner().getId().equals(owner.getId()) ||
                !card2.getOwner().getId().equals(owner.getId())) {
            throw new AppException("Card does not belong to the user", HttpStatus.FORBIDDEN);
        }
    }

    @Transactional
    public void transferBetweenOwnCards(TransferRequest request, String login) {
        Long firstId = Math.min(request.getFromCardId(), request.getToCardId());
        Long secondId = Math.max(request.getFromCardId(), request.getToCardId());

        Card from = cardsRepository.findByIdWithLock(firstId);
        Card to = cardsRepository.findByIdWithLock(secondId);

        User owner = userService.getUserByLogin(login);
        validateCardsOwnership(from, to, owner);

        validateSufficientBalance(from, request.getAmount());

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));

        cardsRepository.save(from);
        cardsRepository.save(to);
    }

    private void validateSufficientBalance(Card card, BigDecimal amount) {
        if (card.getBalance().compareTo(amount) < 0) {
            throw new AppException("Insufficient funds", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long cardId, String login) {
        Card card = getCardAndValidateOwnership(cardId, login);
        return card.getBalance();
    }
}