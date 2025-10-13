package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.entity.card.Card;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    //@Mapping(target = "maskedCardNumber", expression = "java(MaskingUtil.maskCard(card.getCardNumber()))")
    CardDto toDto(Card card);

    @InheritInverseConfiguration
    @Mapping(target = "cardNumber", ignore = true)
    Card toEntity(CardDto dto);
}
