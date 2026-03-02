package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DownloadActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _cardToDownloadFilter;
    private final FilterBlueprint _destinationFilter;

    DownloadActionBlueprint(@JsonProperty(value = "filter", required = true) FilterBlueprint cardToDownloadFilter,
                            @JsonProperty(value = "destinationFilter") FilterBlueprint destinationFilter) {
        _cardToDownloadFilter = new AndFilterBlueprint(cardToDownloadFilter, new YouCanDownloadFilterBlueprint());
        _destinationFilter = Objects.requireNonNullElse(destinationFilter, new AnyCardFilterBlueprint());
    }

    public DownloadCardAction createAction(DefaultGame cardGame, ActionWithSubActions action,
                                           GameTextContext actionContext) {
        String performingPlayerName = actionContext.yourName();
        Collection<PhysicalCard> downloadableCards =
                Filters.filter(cardGame, _cardToDownloadFilter.getFilterable(cardGame, actionContext));
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        for (PhysicalCard downloadableCard : downloadableCards) {
            Collection<PhysicalCard> destinationOptions =
                    Filters.filter(downloadableCard.getDestinationOptionsFromGameText(actionContext, cardGame),
            cardGame, _destinationFilter.getFilterable(cardGame, actionContext));

            if (!destinationOptions.isEmpty()) {
                destinationMap.put(downloadableCard, destinationOptions);
            }
        }
        if (!destinationMap.isEmpty()) {
            EnterPlayAtDestinationResolver resolver =
                    new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
            return new DownloadCardAction(cardGame, performingPlayerName, resolver, actionContext.card(), actionContext);
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