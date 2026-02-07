package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.NullifyCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class NullifySubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _cardTargetBlueprint;

    private NullifySubActionBlueprint(
            @JsonProperty(value = "target")
            TargetResolverBlueprint cardTargetBlueprint
) {
        _cardTargetBlueprint = cardTargetBlueprint;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        ActionCardResolver cardResolver = _cardTargetBlueprint.getTargetResolver(cardGame, context);
        Action nullifyAction = new NullifyCardAction(cardGame, context.card(),
                context.getPerformingPlayerId(), cardResolver);
        return List.of(nullifyAction);
    }

}