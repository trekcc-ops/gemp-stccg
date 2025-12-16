package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class UseGameTextAction extends ActionWithSubActions implements TopLevelSelectableAction {

    protected final PhysicalCard _performingCard;

    public UseGameTextAction(DefaultGame cardGame, PhysicalCard physicalCard, ActionContext context) {
        super(cardGame, physicalCard.getOwnerName(), ActionType.USE_GAME_TEXT, context);
        _performingCard = physicalCard;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }

}