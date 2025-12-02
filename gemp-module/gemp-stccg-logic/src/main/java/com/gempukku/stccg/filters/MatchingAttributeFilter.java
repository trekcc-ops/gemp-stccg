package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class MatchingAttributeFilter implements CardFilter {

    private final PersonnelCard _cardToMatch;
    public MatchingAttributeFilter(PersonnelCard cardToMatch) {
        _cardToMatch = cardToMatch;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof PersonnelCard matchingPersonnel && (
                Objects.equals(matchingPersonnel.getAttribute(CardAttribute.INTEGRITY), _cardToMatch.getAttribute(CardAttribute.INTEGRITY)) ||
                        Objects.equals(matchingPersonnel.getAttribute(CardAttribute.CUNNING), _cardToMatch.getAttribute(CardAttribute.CUNNING)) ||
                        Objects.equals(matchingPersonnel.getAttribute(CardAttribute.STRENGTH), _cardToMatch.getAttribute(CardAttribute.STRENGTH)));
    }
}