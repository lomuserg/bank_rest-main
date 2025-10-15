package com.example.bankcards.controller;

import com.example.bankcards.controller.card.AdminCardController;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminCardController adminCardController;

    @Test
    void createCard_ValidRequest_ShouldReturnCard() {
        CreateCardRequest request = CreateCardRequest.builder()
                .ownerId(1L)
                .expiryDate(LocalDate.now().plusYears(3))
                .balance(BigDecimal.valueOf(1000))
                .build();
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .build();

        when(cardService.createCard(request)).thenReturn(cardDto);

        ResponseEntity<CardDto> response = adminCardController.createCard(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(cardDto, response.getBody());
        verify(cardService).createCard(request);
    }

    @Test
    void getAllCards_WithStatus_ShouldReturnCards() {
        Pageable pageable = Pageable.ofSize(10);
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .build();
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDto));

        when(cardService.getAllCards(CardStatus.ACTIVE, pageable)).thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = adminCardController.getAllCards(CardStatus.ACTIVE, pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
        verify(cardService).getAllCards(CardStatus.ACTIVE, pageable);
    }

    @Test
    void getAllCards_WithoutStatus_ShouldReturnAllCards() {
        Pageable pageable = Pageable.ofSize(10);
        CardDto cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .build();
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDto));

        when(cardService.getAllCards(null, pageable)).thenReturn(cardPage);

        ResponseEntity<Page<CardDto>> response = adminCardController.getAllCards(null, pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
        verify(cardService).getAllCards(null, pageable);
    }

    @Test
    void deleteCard_ExistingCard_ShouldReturnNoContent() {
        doNothing().when(cardService).deleteCard(1L);

        ResponseEntity<?> response = adminCardController.deleteCard(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(cardService).deleteCard(1L);
    }

    @Test
    void blockCard_ExistingCard_ShouldReturnOk() {
        doNothing().when(cardService).blockCard(1L);

        ResponseEntity<?> response = adminCardController.blockCard(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(cardService).blockCard(1L);
    }

    @Test
    void activateCard_ExistingCard_ShouldReturnOk() {
        doNothing().when(cardService).activateCard(1L);

        ResponseEntity<?> response = adminCardController.activateCard(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(cardService).activateCard(1L);
    }
}
