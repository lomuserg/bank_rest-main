package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

    // POST /admin/cards/create — создание новой карты
    @PostMapping("/create")
    public ResponseEntity<CardDto> createCard(@RequestBody CreateCardRequest request) {
        CardDto card = cardService.createCard(request);
        return ResponseEntity.ok(card);
    }

    // GET /admin/cards — просмотр ВСЕХ карт (с фильтрацией и пагинацией)
    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(required = false) CardStatus status,
            Pageable pageable) {
        Page<CardDto> cards = cardService.getAllCards(status, pageable);
        return ResponseEntity.ok(cards);
    }

    // POST /admin/cards/{id}/block — принудительная блокировка
    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    // POST /admin/cards/{id}/activate — разблокировка / активация
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateCard(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }

    // DELETE /admin/cards/{id} — удаление карты
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
