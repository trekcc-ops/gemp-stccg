package com.gempukku.stccg.decisions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;

import java.util.List;

public interface CardSelectionDecision extends AwaitingDecision {
    String[] getCardIds();

    List<? extends PhysicalCard> getSelectableCards();
    void decisionMade(List<? extends PhysicalCard> cardsSelected) throws DecisionResultInvalidException;

}