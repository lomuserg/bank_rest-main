package com.example.bankcards.controller;

import com.example.bankcards.controller.card.CardController;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    @Test
    void getUserCards_WithStatus_ShouldReturnCards() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .build();
        Pageable pageable = Pageable.ofSize(10);
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .build();
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDto));

        when(cardService.getUserCards("testuser", CardStatus.ACTIVE, pageable)).thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = cardController.getUserCards(CardStatus.ACTIVE, pageable, userDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
        verify(cardService).getUserCards("testuser", CardStatus.ACTIVE, pageable);
    }

    @Test
    void getUserCards_WithoutStatus_ShouldReturnCards() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .build();
        Pageable pageable = Pageable.ofSize(10);
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .build();
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDto));

        when(cardService.getUserCards("testuser", null, pageable)).thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = cardController.getUserCards(null, pageable, userDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
        verify(cardService).getUserCards("testuser", null, pageable);
    }

    @Test
    void searchCardsByLastFourDigits_ValidDigits_ShouldReturnCards() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .build();
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .build();
        List<CardDto> cards = List.of(cardDto);

        when(cardService.getUserCardsByLastFourDigits("testuser", "1234")).thenReturn(cards);

        ResponseEntity<List<CardDto>> response = cardController.searchCardsByLastFourDigits("1234", userDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(cardService).getUserCardsByLastFourDigits("testuser", "1234");
    }

    @Test
    void getBalance_ValidCard_ShouldReturnBalance() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .build();

        when(cardService.getBalance(1L, "testuser")).thenReturn(BigDecimal.valueOf(1000));

        ResponseEntity<BigDecimal> response = cardController.getBalance(1L, userDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(1000), response.getBody());
        verify(cardService).getBalance(1L, "testuser");
    }

    @Test
    void transferMoney_ValidRequest_ShouldReturnOk() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .build();
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(500));

        doNothing().when(cardService).transferBetweenOwnCards(request, "testuser");

        ResponseEntity<?> response = cardController.transferMoney(request, userDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(cardService).transferBetweenOwnCards(request, "testuser");
    }
}