package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.CardTargetBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class StopActionBlueprint implements SubActionBlueprint {

    private final CardTargetBlueprint _targetResolver;
    private final String _saveToMemoryId;

    public StopActionBlueprint(@JsonProperty(value = "target")
                               CardTargetBlueprint target,
                               @JsonProperty(value = "saveToMemoryId")
                               String saveToMemoryId) {
        _targetResolver = target;
        _saveToMemoryId = saveToMemoryId;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        ActionCardResolver cardTarget = _targetResolver.getTargetResolver(cardGame, context);
        return List.of(new StopCardsAction(cardGame, context.getPerformingPlayerId(), cardTarget, context, _saveToMemoryId));
    }

}