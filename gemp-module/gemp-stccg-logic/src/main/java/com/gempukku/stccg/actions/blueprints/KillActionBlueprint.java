package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.discard.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class KillActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _targetResolver;

    public KillActionBlueprint(@JsonProperty(value = "target")
                               TargetResolverBlueprint target) {
        _targetResolver = target;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context) {
        String performingPlayerId = (context instanceof DilemmaEncounterGameTextContext) ?
            context.card().getOwnerName() : context.yourName();
        return List.of(
                new KillSinglePersonnelAction(cardGame, performingPlayerId,
                        context.card(), _targetResolver.getTargetResolver(cardGame, context)));
    }

}