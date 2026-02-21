package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.playcard.DownloadMultipleReportablesActionNew;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.ReportCardsResolverBlueprint;
import com.gempukku.stccg.actions.targetresolver.ReportMultipleCardsResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class DownloadReportableActionBlueprint implements SubActionBlueprint {

    private final ReportCardsResolverBlueprint _cardTarget;

    DownloadReportableActionBlueprint(@JsonProperty(value = "target")
                                      ReportCardsResolverBlueprint cardTarget) {
        cardTarget.addReportableFilter(new YouCanDownloadFilterBlueprint());
        _cardTarget = cardTarget;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        List<Action> result = new ArrayList<>();
        ActionCardResolver cardResolver = _cardTarget.getTargetResolver(cardGame, actionContext);
        if (cardResolver instanceof ReportMultipleCardsResolver multipleResolver) {
            Action downloadAction =
                    new DownloadMultipleReportablesActionNew(cardGame, Zone.DRAW_DECK,
                            actionContext.yourName(), actionContext.card(), multipleResolver);
            result.add(downloadAction);
        }
        return result;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) {
        return _cardTarget.canBeResolved(cardGame, actionContext);
    }
}