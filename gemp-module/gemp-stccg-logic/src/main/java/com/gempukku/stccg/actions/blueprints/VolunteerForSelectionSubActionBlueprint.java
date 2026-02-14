package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.choose.RandomSelectionInitiatedResult;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.actions.choose.VolunteerForSelectionAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class VolunteerForSelectionSubActionBlueprint implements SubActionBlueprint {
    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        List<Action> result = new ArrayList<>();
        if (cardGame.getCurrentAction() instanceof SelectRandomCardAction selectAction) {
            result.add(new VolunteerForSelectionAction(cardGame, actionContext.getPerformingPlayerId(),
                    actionContext.card(), selectAction));
        }
        return result;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) {
        return cardGame.getCurrentActionResult() instanceof RandomSelectionInitiatedResult result &&
                result.includesCardMatchingFilter(cardGame, Filters.card(actionContext.card()));
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }
}