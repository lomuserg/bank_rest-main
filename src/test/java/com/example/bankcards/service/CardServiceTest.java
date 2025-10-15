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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardsRepository cardsRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private DebitCardGenerator debitCardGenerator;

    @Mock
    private CardNumberCryptoUtil cryptoUtil;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");

        testCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted1234567890123456")
                .cardHash("hash123")
                .owner(testUser)
                .expiryDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();

        createCardRequest = CreateCardRequest.builder()
                .ownerId(1L)
                .expiryDate(LocalDate.now().plusYears(3))
                .balance(BigDecimal.valueOf(500))
                .build();
    }

    @Test
    void createCard_WithBalance_ShouldCreateCardSuccessfully() {
        String generatedCardNumber = "1234567890123456";
        String encryptedNumber = "encrypted1234567890123456";
        CardDto expectedDto = new CardDto();
        expectedDto.setMaskedCardNumber("**** **** **** 3456");

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(debitCardGenerator.generateUniqueCardNumber()).thenReturn(generatedCardNumber);
        when(cryptoUtil.encrypt(generatedCardNumber)).thenReturn(encryptedNumber);
        when(cardsRepository.save(any(Card.class))).thenReturn(testCard);
        when(cryptoUtil.decrypt(encryptedNumber)).thenReturn(generatedCardNumber);
        when(cardMapper.toDto(testCard)).thenReturn(expectedDto);

        CardDto result = cardService.createCard(createCardRequest);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(userService).getUserById(1L);
        verify(debitCardGenerator).generateUniqueCardNumber();
        verify(cryptoUtil).encrypt(generatedCardNumber);
        verify(cardsRepository).save(any(Card.class));
    }

    @Test
    void createCard_WithoutBalance_ShouldUseZeroBalance() {
        CreateCardRequest requestWithoutBalance = CreateCardRequest.builder()
                .ownerId(1L)
                .expiryDate(LocalDate.now().plusYears(3))
                .build();

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(debitCardGenerator.generateUniqueCardNumber()).thenReturn("1234567890123456");
        when(cryptoUtil.encrypt(anyString())).thenReturn("encrypted");
        when(cardsRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, card.getBalance());
            return testCard;
        });
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());

        cardService.createCard(requestWithoutBalance);

        verify(cardsRepository).save(any(Card.class));
    }

    @Test
    void activateCard_ActiveCard_ShouldThrowException() {
        testCard.setStatus(CardStatus.ACTIVE);
        when(cardsRepository.findById(1L)).thenReturn(testCard);

        AppException exception = assertThrows(AppException.class, () ->
                cardService.activateCard(1L));
        assertEquals("Card is already active", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void activateCard_ExpiredCard_ShouldThrowException() {
        testCard.setStatus(CardStatus.EXPIRED);
        when(cardsRepository.findById(1L)).thenReturn(testCard);

        AppException exception = assertThrows(AppException.class, () ->
                cardService.activateCard(1L));
        assertEquals("Cannot activate an expired card", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void activateCard_BlockedCard_ShouldActivateSuccessfully() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardsRepository.findById(1L)).thenReturn(testCard);

        cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, testCard.getStatus());
        verify(cardsRepository).save(testCard);
    }

    @Test
    void blockCard_AlreadyBlocked_ShouldThrowException() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardsRepository.findById(1L)).thenReturn(testCard);

        AppException exception = assertThrows(AppException.class, () ->
                cardService.blockCard(1L));
        assertEquals("Card is already blocked", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void blockCard_ActiveCard_ShouldBlockSuccessfully() {
        testCard.setStatus(CardStatus.ACTIVE);
        when(cardsRepository.findById(1L)).thenReturn(testCard);

        cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardsRepository).save(testCard);
    }

    @Test
    void deleteCard_ExistingCard_ShouldDeleteSuccessfully() {
        when(cardsRepository.findById(1L)).thenReturn(testCard);

        cardService.deleteCard(1L);

        verify(cardsRepository).deleteById(1L);
    }

    @Test
    void getAllCards_WithStatusFilter_ShouldReturnFilteredCards() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardsRepository.findAllByStatus(CardStatus.ACTIVE, pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        Page<CardDto> result = cardService.getAllCards(CardStatus.ACTIVE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardsRepository).findAllByStatus(CardStatus.ACTIVE, pageable);
    }

    @Test
    void getAllCards_WithoutStatus_ShouldReturnAllCards() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardsRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        Page<CardDto> result = cardService.getAllCards(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardsRepository).findAll(pageable);
    }

    @Test
    void getUserCards_WithStatus_ShouldReturnUserCards() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);
        when(cardsRepository.findPageByOwnerIdAndStatus(1L, CardStatus.ACTIVE, pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());
        when(cryptoUtil.decrypt(anyString())).thenReturn("1234567890123456");

        Page<CardDto> result = cardService.getUserCards("testuser", CardStatus.ACTIVE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardsRepository).findPageByOwnerIdAndStatus(1L, CardStatus.ACTIVE, pageable);
    }

    @Test
    void getUserCardsByLastFourDigits_ValidDigits_ShouldReturnMatchingCards() {
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);
        when(cardsRepository.findAllByOwnerId(1L)).thenReturn(List.of(testCard));
        when(cryptoUtil.decrypt("encrypted1234567890123456")).thenReturn("1234567890123456");
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());

        List<CardDto> result = cardService.getUserCardsByLastFourDigits("testuser", "3456");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserCardsByLastFourDigits_InvalidLength_ShouldThrowException() {
        AppException exception = assertThrows(AppException.class, () ->
                cardService.getUserCardsByLastFourDigits("testuser", "345"));

        assertEquals("Last 4 digits must consist of 4 characters", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getUserCardsByLastFourDigits_NonNumeric_ShouldThrowException() {
        AppException exception = assertThrows(AppException.class, () ->
                cardService.getUserCardsByLastFourDigits("testuser", "34a6"));

        assertEquals("Last 4 digits must contain only numbers", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void requestBlockCard_NotOwner_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setLogin("otheruser");

        when(cardsRepository.findById(1L)).thenReturn(testCard);
        when(userService.getUserByLogin("otheruser")).thenReturn(otherUser);

        AppException exception = assertThrows(AppException.class, () ->
                cardService.requestBlockCard(1L, "otheruser"));

        assertEquals("Card does not belong to the user", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void transferBetweenOwnCards_ValidTransfer_ShouldTransferSuccessfully() {
        Card fromCard = Card.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(1000))
                .owner(testUser)
                .build();
        Card toCard = Card.builder()
                .id(2L)
                .balance(BigDecimal.valueOf(500))
                .owner(testUser)
                .build();

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(300));

        when(cardsRepository.findByIdWithLock(1L)).thenReturn(fromCard);
        when(cardsRepository.findByIdWithLock(2L)).thenReturn(toCard);
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);

        cardService.transferBetweenOwnCards(request, "testuser");

        assertEquals(BigDecimal.valueOf(700), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(800), toCard.getBalance());
        verify(cardsRepository).save(fromCard);
        verify(cardsRepository).save(toCard);
    }

    @Test
    void transferBetweenOwnCards_InsufficientBalance_ShouldThrowException() {
        Card fromCard = Card.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .owner(testUser)
                .build();
        Card toCard = Card.builder()
                .id(2L)
                .balance(BigDecimal.valueOf(500))
                .owner(testUser)
                .build();

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(300));

        when(cardsRepository.findByIdWithLock(1L)).thenReturn(fromCard);
        when(cardsRepository.findByIdWithLock(2L)).thenReturn(toCard);
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);

        AppException exception = assertThrows(AppException.class, () ->
                cardService.transferBetweenOwnCards(request, "testuser"));

        assertEquals("Insufficient funds", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getBalance_ValidCard_ShouldReturnBalance() {
        when(cardsRepository.findById(1L)).thenReturn(testCard);
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);

        BigDecimal result = cardService.getBalance(1L, "testuser");

        assertEquals(BigDecimal.valueOf(1000), result);
    }

    @Test
    void getBalance_NotOwner_ShouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setLogin("otheruser");

        when(cardsRepository.findById(1L)).thenReturn(testCard);
        when(userService.getUserByLogin("otheruser")).thenReturn(otherUser);

        AppException exception = assertThrows(AppException.class, () ->
                cardService.getBalance(1L, "otheruser"));

        assertEquals("Card does not belong to the user", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }
}
