package com.example.bankcards.service;

import com.example.bankcards.repository.CardsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardsRepository cardsRepository;
    private final UserService userService;

}
