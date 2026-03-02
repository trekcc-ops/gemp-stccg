package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Objects;

public class StopSubActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _targetResolver;
    private final String _saveToMemoryId;
    private final Requirement _requirement;

    @JsonCreator
    private StopSubActionBlueprint(@JsonProperty(value = "target")
                               TargetResolverBlueprint target,
                                  @JsonProperty(value = "saveToMemoryId")
                               String saveToMemoryId,
                                  @JsonProperty(value = "requires")
                               Requirement requirement)
            {
        _targetResolver = target;
        _saveToMemoryId = Objects.requireNonNullElse(saveToMemoryId, "temp");
        _requirement = requirement;
    }

    public StopCardsAction createAction(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context) {
        String performingPlayerId = (context instanceof DilemmaEncounterGameTextContext) ?
                context.card().getOwnerName() : context.yourName();
        if (_requirement == null || _requirement.accepts(context, cardGame)) {
            ActionCardResolver cardTarget = _targetResolver.getTargetResolver(cardGame, context);
            return new StopCardsAction(cardGame, performingPlayerId, cardTarget, context, _saveToMemoryId);
        } else {
            return null;
        }
    }

}