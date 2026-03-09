package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.choose.MakeDecisionAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.ReportCardsResolverBlueprint;
import com.gempukku.stccg.actions.targetresolver.ReportMultipleCardsResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

public class DownloadReportableActionBlueprint implements SubActionBlueprint {

    private final ReportCardsResolverBlueprint _cardTarget;
    private final boolean _optional;

    DownloadReportableActionBlueprint(@JsonProperty(value = "target")
                                      ReportCardsResolverBlueprint cardTarget,
                                      @JsonProperty(value = "optional") boolean optional) {
        cardTarget.addReportableFilter(new YouCanDownloadFilterBlueprint());
        _cardTarget = cardTarget;
        _optional = optional;
    }

    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        ActionCardResolver cardResolver = _cardTarget.getTargetResolver(cardGame, context);
        if (cardResolver instanceof ReportMultipleCardsResolver multipleResolver) {
            if (_optional) {
                return new MakeDecisionAction(cardGame, context.yourName(), "", context) {

                    @Override
                    protected AwaitingDecision getDecision(DefaultGame cardGame1) {
                        int maxCards = _cardTarget.getMaximumCards(cardGame, context);
                        String choiceText = "Do you want to download ";
                        if (maxCards == 1) {
                            choiceText = choiceText + " a card ";
                        } else {
                            choiceText = choiceText + " cards ";
                        }
                        choiceText = choiceText + " using " + context.card().getTitle() + "?";
                        return new YesNoDecision(context.yourName(), choiceText, cardGame1) {
                            @Override
                            protected void yes() {
                                cardGame1.addActionToStack(
                                        new DownloadMultipleReportablesActionNew(cardGame, Zone.DRAW_DECK, context.yourName(),
                                                context.card(), multipleResolver));
                            }

                            @Override
                            protected void no() {

                            }
                        };
                    }
                };
            } else {
                return new DownloadMultipleReportablesActionNew(cardGame, Zone.DRAW_DECK, context.yourName(),
                        context.card(), multipleResolver);
            }
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