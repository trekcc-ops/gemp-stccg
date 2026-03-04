package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.DownloadMultipleReportablesActionNew;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.ReportCardsResolverBlueprint;
import com.gempukku.stccg.actions.targetresolver.ReportMultipleCardsResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

public class DownloadReportableActionBlueprint implements SubActionBlueprint {

    private final ReportCardsResolverBlueprint _cardTarget;

    DownloadReportableActionBlueprint(@JsonProperty(value = "target")
                                      ReportCardsResolverBlueprint cardTarget) {
        cardTarget.addReportableFilter(new YouCanDownloadFilterBlueprint());
        _cardTarget = cardTarget;
    }

    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        ActionCardResolver cardResolver = _cardTarget.getTargetResolver(cardGame, context);
        if (cardResolver instanceof ReportMultipleCardsResolver multipleResolver) {
            return new DownloadMultipleReportablesActionNew(cardGame, Zone.DRAW_DECK, context.yourName(),
                    context.card(), multipleResolver);
        } else {
            return null;
        }
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