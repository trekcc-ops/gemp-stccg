package com.gempukku.stccg.decisions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;

import java.util.List;

public interface CardSelectionDecision {
    public String[] getCardIds();

    public List<? extends PhysicalCard> getSelectableCards();

    public void setDecisionResponse(List<PhysicalCard> cards) throws DecisionResultInvalidException;

    void followUp() throws DecisionResultInvalidException, InvalidGameLogicException;

}