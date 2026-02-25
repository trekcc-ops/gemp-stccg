package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.RemoveCardFromPlayAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class RemoveCardsFromGameSubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _target;

    @JsonCreator
    private RemoveCardsFromGameSubActionBlueprint(@JsonProperty(value = "target", required = true)
                                                      TargetResolverBlueprint target) {
        _target = target;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      GameTextContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        List<Action> result = new ArrayList<>();
        if (_target.canBeResolved(cardGame, actionContext) &&
                _target.getTargetResolver(cardGame, actionContext) != null) {
            ActionCardResolver resolver = _target.getTargetResolver(cardGame, actionContext);
            Action removeAction = new RemoveCardFromPlayAction(cardGame, actionContext.yourName(), resolver);
            result.add(removeAction);
        }
        return result;
    }
}