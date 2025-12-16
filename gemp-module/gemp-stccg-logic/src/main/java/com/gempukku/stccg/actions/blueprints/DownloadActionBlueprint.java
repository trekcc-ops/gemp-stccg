package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.targetresolver.CardTargetBlueprint;
import com.gempukku.stccg.actions.targetresolver.SelectCardTargetBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class DownloadActionBlueprint implements SubActionBlueprint {

    private final CardTargetBlueprint _cardTarget;

    DownloadActionBlueprint(@JsonProperty(value = "target")
                            CardTargetBlueprint cardTarget) {
        _cardTarget = cardTarget;
        if (_cardTarget instanceof SelectCardTargetBlueprint selectBlueprint) {
            selectBlueprint.addFilter(new YouCanDownloadFilterBlueprint());
        }
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        Action downloadAction = new DownloadCardAction(cardGame, actionContext.getPerformingPlayerId(),
                _cardTarget.getTargetResolver(cardGame, actionContext), actionContext.getPerformingCard(cardGame));
        return List.of(downloadAction);
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) {
        return _cardTarget.canBeResolved(cardGame, actionContext);
    }
}