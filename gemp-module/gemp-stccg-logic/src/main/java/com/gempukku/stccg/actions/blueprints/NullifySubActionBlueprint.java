package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.actions.discard.NullifyCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class NullifySubActionBlueprint implements SubActionBlueprint {

    private final CardTargetBlueprint _cardTargetBlueprint;

    private NullifySubActionBlueprint(
            @JsonProperty(value = "target")
            CardTargetBlueprint cardTargetBlueprint
) {
        _cardTargetBlueprint = cardTargetBlueprint;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        ActionCardResolver cardResolver = _cardTargetBlueprint.getTargetResolver(cardGame, context);
        Action nullifyAction = new NullifyCardAction(cardGame, context.card(),
                context.getPerformingPlayerId(), cardResolver);
        return List.of(nullifyAction);
    }

}