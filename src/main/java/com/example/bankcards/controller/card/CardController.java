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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // GET /cards — просмотр своих карт (с пагинацией)
    @GetMapping
    public ResponseEntity<Page<CardDto>> getMyCards(
            @RequestParam(required = false) CardStatus status,
            Pageable pageable,
            @AuthenticationPrincipal UserDto userDto) {

        Page<CardDto> cards = cardService.getUserCards(userDto.getLogin(), status, pageable);
        return ResponseEntity.ok(cards);
    }

    // POST /cards/block-request — запрос на блокировку своей карты
    @PostMapping("/block-request")
    public ResponseEntity<?> requestBlockCard(
            @RequestParam Long cardId,
            @AuthenticationPrincipal UserDto userDto) {

        cardService.requestBlockCard(cardId, userDto.getLogin());
        return ResponseEntity.ok("Запрос на блокировку отправлен");
    }

    // POST /cards/transfer — перевод между своими картами
    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestBody @Valid TransferRequest request,
            @AuthenticationPrincipal UserDto userDto) {

        cardService.transferBetweenOwnCards(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount(),
                userDto.getLogin()
        );
        return ResponseEntity.ok().build();
    }

    // GET /cards/balance/{id} — просмотр баланса своей карты
    @GetMapping("/balance/{cardId}")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserDto userDto) {

        BigDecimal balance = cardService.getBalance(cardId, userDto.getLogin());
        return ResponseEntity.ok(balance);
    }
}
