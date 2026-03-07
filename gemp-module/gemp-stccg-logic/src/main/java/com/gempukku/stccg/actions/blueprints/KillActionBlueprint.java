package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.discard.KillAction;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class KillActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _targetResolver;

    public KillActionBlueprint(@JsonProperty(value = "target")
                               TargetResolverBlueprint target) {
        _targetResolver = target;
    }

    public KillAction createAction(DefaultGame cardGame, GameTextContext context) {
        String performingPlayerId = (context instanceof DilemmaEncounterGameTextContext) ?
            context.card().getOwnerName() : context.yourName();
        if (_targetResolver.canBeResolved(cardGame, context)) {
            return new KillAction(cardGame, performingPlayerId, context.card(),
                    _targetResolver.getTargetResolver(cardGame, context));
        } else {
            return null;
        }
    }

}