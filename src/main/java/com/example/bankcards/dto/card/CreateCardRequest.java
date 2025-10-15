package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;
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
public class CreateCardRequest {

    @NotNull
    private LocalDate expiryDate;

    @PositiveOrZero
    private BigDecimal balance;

    @NotNull
    private Long ownerId;
}
