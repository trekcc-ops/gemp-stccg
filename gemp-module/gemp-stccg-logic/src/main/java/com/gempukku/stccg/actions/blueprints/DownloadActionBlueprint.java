package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class DownloadActionBlueprint implements SubActionBlueprint {

    private final String _saveToMemoryId;
    private final CardTargetBlueprint _cardTarget;

    DownloadActionBlueprint(@JsonProperty(value = "saveToMemoryId")
                            String saveToMemoryId,
                            @JsonProperty(value = "target")
                            CardTargetBlueprint cardTarget) {
        _cardTarget = cardTarget;
        _cardTarget.addFilter(new YouCanDownloadFilterBlueprint());
        _saveToMemoryId = (saveToMemoryId == null) ? "_temp" : saveToMemoryId;
    }

    @Override
    public List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        Action downloadAction = new DownloadCardAction(actionContext.getGame(), actionContext.getPerformingPlayer(),
                _cardTarget.getTargetResolver(actionContext), actionContext.getSource());
        return List.of(downloadAction);
    }
}