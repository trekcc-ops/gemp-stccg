package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class StopActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _targetResolver;
    private final String _saveToMemoryId;
    private final Requirement _requirement;

    public StopActionBlueprint(@JsonProperty(value = "target")
                               TargetResolverBlueprint target,
                               @JsonProperty(value = "saveToMemoryId")
                               String saveToMemoryId,
                               @JsonProperty(value = "requires")
                               Requirement requirement)
            {
        _targetResolver = target;
        _saveToMemoryId = saveToMemoryId;
        _requirement = requirement;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        String performingPlayerId = (context instanceof DilemmaEncounterGameTextContext) ?
                context.card().getOwnerName() : context.yourName();
        List<Action> result = new ArrayList<>();
        if (_requirement == null || _requirement.accepts(context, cardGame)) {
            ActionCardResolver cardTarget = _targetResolver.getTargetResolver(cardGame, context);
            result.add(new StopCardsAction(cardGame, performingPlayerId, cardTarget, context, _saveToMemoryId));
        }
        return result;
    }

}