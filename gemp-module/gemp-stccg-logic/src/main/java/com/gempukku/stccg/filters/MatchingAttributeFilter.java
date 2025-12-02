package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
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
                return physicalCard instanceof PersonnelCard matchingPersonnel && (
                        Objects.equals(matchingPersonnel.getAttribute(CardAttribute.INTEGRITY), personnelToMatch.getAttribute(CardAttribute.INTEGRITY)) ||
                                Objects.equals(matchingPersonnel.getAttribute(CardAttribute.CUNNING), personnelToMatch.getAttribute(CardAttribute.CUNNING)) ||
                                Objects.equals(matchingPersonnel.getAttribute(CardAttribute.STRENGTH), personnelToMatch.getAttribute(CardAttribute.STRENGTH)));
            } else {
                return false;
            }
        } catch(CardNotFoundException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }
}