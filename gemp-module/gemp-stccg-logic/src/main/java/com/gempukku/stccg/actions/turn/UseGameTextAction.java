package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class UseGameTextAction extends ActionWithSubActions implements CardPerformedAction {

    protected final PhysicalCard _performingCard;

    public UseGameTextAction(DefaultGame cardGame, PhysicalCard physicalCard, GameTextContext context) {
        super(cardGame, physicalCard.getOwnerName(), ActionType.USE_GAME_TEXT, context);
        _performingCard = physicalCard;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }

    public Action getCurrentSubAction() {
        return _currentSubAction;
    }

}