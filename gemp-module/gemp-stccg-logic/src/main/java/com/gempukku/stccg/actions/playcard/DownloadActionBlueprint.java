package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.choose.MakeDecisionAction;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DownloadActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _cardToDownloadFilter;
    private final FilterBlueprint _destinationFilter;
    private final boolean _optional;

    DownloadActionBlueprint(@JsonProperty(value = "filter", required = true) FilterBlueprint cardToDownloadFilter,
                            @JsonProperty(value = "destinationFilter") FilterBlueprint destinationFilter,
                            @JsonProperty("optional") boolean optional) {
        _cardToDownloadFilter = new AndFilterBlueprint(cardToDownloadFilter, new YouCanDownloadFilterBlueprint());
        _destinationFilter = Objects.requireNonNullElse(destinationFilter, new AnyCardFilterBlueprint());
        _optional = optional;
    }

    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        String performingPlayerName = context.yourName();
        Collection<PhysicalCard> downloadableCards =
                Filters.filter(cardGame, _cardToDownloadFilter.getFilterable(cardGame, context));
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        for (PhysicalCard downloadableCard : downloadableCards) {
            Collection<PhysicalCard> destinationOptions =
                    Filters.filter(downloadableCard.getDestinationOptionsFromGameText(context, cardGame),
            cardGame, _destinationFilter.getFilterable(cardGame, context));

            if (!destinationOptions.isEmpty()) {
                destinationMap.put(downloadableCard, destinationOptions);
            }
        }
        if (!destinationMap.isEmpty()) {
            EnterPlayAtDestinationResolver resolver =
                    new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
            if (_optional) {
                return new MakeDecisionAction(cardGame, context.yourName(), "", context) {

                    @Override
                    protected AwaitingDecision getDecision(DefaultGame cardGame1) {
                        String choiceText = "Do you want to download a card using ";
                        choiceText = choiceText + context.card().getTitle() + "?";
                        return new YesNoDecision(context.yourName(), choiceText, cardGame1) {
                            @Override
                            protected void yes() {
                                cardGame1.addActionToStack(new DownloadCardAction(cardGame, performingPlayerName, resolver, context.card(), context));
                            }

                            @Override
                            protected void no() {

                            }
                        };
                    }
                };
            } else {
                return new DownloadCardAction(cardGame, performingPlayerName, resolver, context.card(), context);
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
        return !Filters.filter(cardGame, _cardToDownloadFilter.getFilterable(cardGame, actionContext)).isEmpty();
    }
}