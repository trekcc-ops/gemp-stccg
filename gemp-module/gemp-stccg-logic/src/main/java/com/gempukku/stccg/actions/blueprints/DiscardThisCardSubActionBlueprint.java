package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class DiscardThisCardSubActionBlueprint implements SubActionBlueprint {

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext actionContext) {
        PhysicalCard performingCard = actionContext.card();
        String playerName = actionContext.getPerformingPlayerId();
        return List.of(new DiscardSingleCardAction(cardGame, performingCard, playerName, performingCard));
    }
}