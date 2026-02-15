package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class MatchingAttributeFilter implements CardFilter {

    @JsonProperty("cardId")
    private final int _cardId;
    public MatchingAttributeFilter(PersonnelCard cardToMatch) {
        _cardId = cardToMatch.getCardId();
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        try {
            PhysicalCard cardToMatch = game.getCardFromCardId(_cardId);
            if (cardToMatch instanceof PersonnelCard personnelToMatch) {
                return physicalCard instanceof PersonnelCard matchingPersonnel &&
                        personnelMatchAtLeastOneAttribute(personnelToMatch, matchingPersonnel, game);
            } else {
                return false;
            }
        } catch(CardNotFoundException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }

    private boolean personnelMatchAtLeastOneAttribute(PersonnelCard card1, PersonnelCard card2, DefaultGame cardGame) {
        if (Objects.equals(card1.getIntegrity(cardGame), card2.getIntegrity(cardGame))) {
            return true;
        }
        if (Objects.equals(card1.getCunning(cardGame), card2.getCunning(cardGame))) {
            return true;
        }
        if (Objects.equals(card1.getStrength(cardGame), card2.getStrength(cardGame))) {
            return true;
        }
        return false;
    }
}