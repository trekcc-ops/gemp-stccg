package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.RemoveCardFromPlayAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class RemoveCardsFromGameSubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _target;

    @JsonCreator
    private RemoveCardsFromGameSubActionBlueprint(@JsonProperty(value = "target", required = true)
                                                      TargetResolverBlueprint target) {
        _target = target;
    }

    @Override
    public RemoveCardFromPlayAction createAction(DefaultGame cardGame, GameTextContext actionContext) {
        if (_target.canBeResolved(cardGame, actionContext) &&
                _target.getTargetResolver(cardGame, actionContext) != null) {
            ActionCardResolver resolver = _target.getTargetResolver(cardGame, actionContext);
            return new RemoveCardFromPlayAction(cardGame, actionContext.yourName(), resolver);
        } else {
            return null;
        }
    }
}