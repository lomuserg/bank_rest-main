package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/v1/cards")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardDto>> getUserCards(
            @RequestParam(required = false) CardStatus status,
            Pageable pageable,
            @AuthenticationPrincipal UserDto userDto) {

        Page<CardDto> cards = cardService.getUserCards(userDto.getLogin(), status, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CardDto>> searchCardsByLastFourDigits(
            @RequestParam String lastFourDigits,
            @AuthenticationPrincipal UserDto userDto) {

        List<CardDto> cards = cardService.getUserCardsByLastFourDigits(
                userDto.getLogin(),
                lastFourDigits
        );
        return ResponseEntity.ok(cards);
    }

    @PostMapping("{cardId}/block")
    public ResponseEntity<?> requestBlockCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserDto userDto) {

        cardService.requestBlockCard(cardId, userDto.getLogin());
        return ResponseEntity.ok("Запрос на блокировку отправлен");
    }

    @GetMapping("{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserDto userDto) {

        BigDecimal balance = cardService.getBalance(cardId, userDto.getLogin());
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestBody @Valid TransferRequest request,
            @AuthenticationPrincipal UserDto userDto) {

        cardService.transferBetweenOwnCards(request, userDto.getLogin());
        return ResponseEntity.ok().build();
    }

}
