package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PlayThisCardAsResponseAction extends UseGameTextAction implements TopLevelSelectableAction {

    public PlayThisCardAsResponseAction(DefaultGame cardGame, PhysicalCard physicalCard, ActionContext context) {
        super(cardGame, physicalCard, context);
    }

    public boolean requirementsAreMet(DefaultGame game) { return _performingCard.isInHand(game); }

}