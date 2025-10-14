package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDto {
    private Long id;
    private String maskedCardNumber;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
