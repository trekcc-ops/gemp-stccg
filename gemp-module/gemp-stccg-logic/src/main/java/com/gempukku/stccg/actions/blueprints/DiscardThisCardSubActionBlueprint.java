package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class DiscardThisCardSubActionBlueprint implements SubActionBlueprint {

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        PhysicalCard performingCard = actionContext.getPerformingCard(cardGame);
        String playerName = actionContext.getPerformingPlayerId();
        return List.of(new DiscardSingleCardAction(cardGame, performingCard, playerName, performingCard));
    }
}