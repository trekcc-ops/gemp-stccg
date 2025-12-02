package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class TurnLimitRequirement implements Requirement {

    private final int _limitPerTurn;

    public TurnLimitRequirement(int limitPerTurn) {
        _limitPerTurn = limitPerTurn;
    }
    public boolean isTrue(PhysicalCard thisCard, DefaultGame cardGame) {
        return thisCard.checkTurnLimit(cardGame, _limitPerTurn);
    }
}