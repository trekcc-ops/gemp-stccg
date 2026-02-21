package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class DiscardSubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _cardTarget;

    public DiscardSubActionBlueprint(
            @JsonProperty("cards")
            TargetResolverBlueprint cardTargetBlueprint
    ) {
        _cardTarget = cardTargetBlueprint;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext actionContext) {
        ActionCardResolver cardResolver = _cardTarget.getTargetResolver(cardGame, actionContext);
        PhysicalCard performingCard = actionContext.card();
        String playerName = actionContext.yourName();
        return List.of(new DiscardSingleCardAction(cardGame, performingCard, playerName, cardResolver));
    }
}