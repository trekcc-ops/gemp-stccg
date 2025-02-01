package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class DiscardThisCardSubActionBlueprint implements SubActionBlueprint {

    @Override
    public List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        return List.of(
                new DiscardCardAction(actionContext.getSource(), actionContext.getPerformingPlayer(),
                        actionContext.getSource()));
    }
}