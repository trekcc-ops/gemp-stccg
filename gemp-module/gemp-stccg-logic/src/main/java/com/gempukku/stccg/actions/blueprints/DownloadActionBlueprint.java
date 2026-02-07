package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardTargetBlueprint;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class DownloadActionBlueprint implements SubActionBlueprint {

    private final TargetResolverBlueprint _cardTarget;

    DownloadActionBlueprint(@JsonProperty(value = "target")
                            TargetResolverBlueprint cardTarget) {
        _cardTarget = cardTarget;
        if (_cardTarget instanceof SelectCardTargetBlueprint selectBlueprint) {
            selectBlueprint.addFilter(new YouCanDownloadFilterBlueprint());
        }
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext actionContext) {
        List<Action> result = new ArrayList<>();
        String performingPlayerName = actionContext.getPerformingPlayerId();
        ActionCardResolver resolver = _cardTarget.getTargetResolver(cardGame, actionContext);
        Action downloadAction = new DownloadCardAction(cardGame, performingPlayerName, resolver, actionContext.card());
        result.add(downloadAction);
        return result;
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