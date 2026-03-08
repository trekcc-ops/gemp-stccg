package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class DiscardSubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _cardTarget;

    public DiscardSubActionBlueprint(
            @JsonProperty("target")
            TargetResolverBlueprint cardTargetBlueprint
    ) {
        _cardTarget = cardTargetBlueprint;
    }

    @Override
    public DiscardSingleCardAction createAction(DefaultGame cardGame, GameTextContext actionContext) {
        ActionCardResolver cardResolver = _cardTarget.getTargetResolver(cardGame, actionContext);
        PhysicalCard performingCard = actionContext.card();
        String playerName = actionContext.yourName();
        return new DiscardSingleCardAction(cardGame, performingCard, playerName, cardResolver);
    }
}