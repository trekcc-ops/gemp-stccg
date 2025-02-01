package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class KillActionBlueprint implements SubActionBlueprint {

    private final CardTargetBlueprint _targetResolver;

    KillActionBlueprint(@JsonProperty(value = "target")
                        CardTargetBlueprint target) {
        _targetResolver = target;
    }

    @Override
    public List<Action> createActions(CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        return List.of(
                new KillSinglePersonnelAction(context.getPerformingPlayer(), context.getSource(),
                        _targetResolver.getTargetResolver(context)));
    }

}