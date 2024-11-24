package com.gempukku.stccg.condition;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class FacingDilemmaCondition implements Condition {
    private final PersonnelCard _card;

    public FacingDilemmaCondition(PhysicalCard card) throws InvalidGameLogicException {
        if (card instanceof PersonnelCard personnel) {
            _card = personnel;
        } else {
            throw new InvalidGameLogicException("Cannot apply FacingDilemmaCondition to a non-personnel card");
        }
    }

    @Override
    public boolean isFulfilled() {
        return _card.isFacingADilemma();
    }
}