package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.NullifyCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class NullifySubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _cardTargetBlueprint;

    private NullifySubActionBlueprint(
            @JsonProperty(value = "target")
            TargetResolverBlueprint cardTargetBlueprint
) {
        _cardTargetBlueprint = cardTargetBlueprint;
    }

    public NullifyCardAction createAction(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context) {
        ActionCardResolver cardResolver = _cardTargetBlueprint.getTargetResolver(cardGame, context);
        return new NullifyCardAction(cardGame, context.card(), context.yourName(), cardResolver);
    }

}