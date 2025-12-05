package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class DiscardSubActionBlueprint implements SubActionBlueprint {

    private final CardTargetBlueprint _cardTarget;

    public DiscardSubActionBlueprint(
            @JsonProperty("cards")
            CardTargetBlueprint cardTargetBlueprint
    ) {
        _cardTarget = cardTargetBlueprint;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        ActionCardResolver cardResolver = _cardTarget.getTargetResolver(cardGame, actionContext);
        PhysicalCard performingCard = actionContext.getPerformingCard(cardGame);
        String playerName = actionContext.getPerformingPlayerId();
        return List.of(new DiscardSingleCardAction(cardGame, performingCard, playerName, cardResolver));
    }
}