package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.actions.SelectCardTargetBlueprint;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
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
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        Action downloadAction = new DownloadCardAction(cardGame, actionContext.getPerformingPlayerId(),
                _cardTarget.getTargetResolver(cardGame, actionContext), actionContext.getPerformingCard(cardGame));
        return List.of(downloadAction);
    }
}